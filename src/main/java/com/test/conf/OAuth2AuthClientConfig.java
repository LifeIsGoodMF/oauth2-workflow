package com.test.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.domain.dto.User;
import com.test.service.RegistrationService;
import com.test.service.UserDetailServiceCustom;
import com.test.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Configuration
@EnableOAuth2Client
public class OAuth2AuthClientConfig {
    static final String LOGIN_FACEBOOK = "/login/facebook";
    static final String LOGIN_GOOGLE = "/login/google";


    private final ObjectMapper objectMapper;
    private final TokenEndpoint tokenEndpoint;
    private final OAuth2ClientContext oauth2ClientContext;
    private final RegistrationService registrationService;
    private final UserDetailServiceCustom userDetailsService;

    @SuppressWarnings("SpringJavaAutowiringInspection") // Provided by Spring Boot
    @Autowired
    public OAuth2AuthClientConfig(ObjectMapper objectMapper, @Lazy TokenEndpoint tokenEndpoint, OAuth2ClientContext oauth2ClientContext,
                                  @Lazy RegistrationService registrationService, @Lazy UserDetailServiceCustom userDetailsService) {
        this.objectMapper = objectMapper;
        this.tokenEndpoint = tokenEndpoint;
        this.oauth2ClientContext = oauth2ClientContext;
        this.registrationService = registrationService;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    @ConfigurationProperties("facebook")
    public ClientResources facebook() {
        return new ClientResources(facebookPrincipalExtractor());
    }

    @Bean
    @ConfigurationProperties("google")
    public ClientResources google() {
        return new ClientResources(googlePrincipalExtractor());
    }

    @Bean
    public Filter googleAuthenticationProcessingFilter() {
        return ssoFilter(google(), LOGIN_GOOGLE);
    }

    @Bean
    public Filter facebookAuthenticationProcessingFilter() {
        return ssoFilter(facebook(), LOGIN_FACEBOOK);
    }

    private Filter ssoFilter(ClientResources client, String path) {
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);
        OAuth2RestTemplate template = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);

        filter.setRestTemplate(template);
        UserInfoTokenServices tokenService = new UserInfoTokenServices(client.getResource().getUserInfoUri(), client.getClient().getClientId());
        tokenService.setPrincipalExtractor(client.getPrincipalExtractor());

        filter.setTokenServices(tokenService);
        filter.setAllowSessionCreation(false);
        filter.setSessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy());
        filter.setAuthenticationSuccessHandler(new SavedRequestAwareAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                String email = authentication.getName();
                UserDetails userDetails = null;
                try {
                    userDetails = userDetailsService.loadUserByUsername(email);
                } catch (UsernameNotFoundException ignored) { }

                if (userDetails == null && !"unknown".equals(email)) {
                    OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) authentication;
                    Map<String, Object> details = (Map<String, Object>) oAuth2Authentication.getUserAuthentication().getDetails();
                    String password = PasswordGenerator.generatePassword(8);
                    User registerUser = registrationService.registerUser(new User((String) details.get("displayName"), password, email, true, false));
                    registrationService.sendConfirmationSuccess(registerUser, password);
                    userDetails = userDetailsService.loadUserByUsername(email);

                    HashMap<String, String> authorizationParameters = new HashMap<>();
                    authorizationParameters.put(OAuth2Utils.SCOPE, "read");
                    authorizationParameters.put("username", email);
                    authorizationParameters.put("password", password);
                    authorizationParameters.put(OAuth2Utils.CLIENT_ID, "system");
                    authorizationParameters.put(OAuth2Utils.GRANT_TYPE, "password");

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("system", null, userDetails.getAuthorities());
                    ResponseEntity<OAuth2AccessToken> accessToken = tokenEndpoint.postAccessToken(authenticationToken, authorizationParameters);

                    objectMapper.writeValue(response.getWriter(), accessToken.getBody());

                } else {
                    doTheMagic();
                }

                super.onAuthenticationSuccess(request, response, authentication);
            }
        });

        return filter;
    }

    private void doTheMagic() {

    }

    @Bean
    public PrincipalExtractor googlePrincipalExtractor() {
        return new GooglePrincipalExtractor();
    }

    @Bean
    public PrincipalExtractor facebookPrincipalExtractor() {
        return new FacebookPrincipalExtractor();
    }

    private static class ClientResources {

        private final PrincipalExtractor principalExtractor;

        ClientResources(PrincipalExtractor principalExtractor) {
            this.principalExtractor = principalExtractor;
        }

        @NestedConfigurationProperty
        private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

        @NestedConfigurationProperty
        private ResourceServerProperties resource = new ResourceServerProperties();

        public AuthorizationCodeResourceDetails getClient() {
            return client;
        }

        public ResourceServerProperties getResource() {
            return resource;
        }

        public PrincipalExtractor getPrincipalExtractor() {
            return principalExtractor;
        }
    }


    private static class GooglePrincipalExtractor implements PrincipalExtractor {
        @Override
        public Object extractPrincipal(Map<String, Object> map) {
            if (map.containsKey("emails")) {
                Collection<Map<String, String>> emails = (Collection<Map<String, String>>) map.get("emails");
                Iterator<Map<String, String>> iterator = emails.iterator();
                if (iterator.hasNext()) {
                    Map<String, String> email = iterator.next();
                    return email.get("value");
                }
            }
            return null;
        }
    }

    private static class FacebookPrincipalExtractor implements PrincipalExtractor {
        @Override
        public Object extractPrincipal(Map<String, Object> map) {
            Object email = map.get("email");
            if (email != null) {
                map.put("displayName", map.get("name"));
                return email;
            }
            return null;
        }
    }
}



