package com.test.conf;

import com.test.service.UserDetailServiceCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.test.conf.OAuth2AuthClientConfig.LOGIN_FACEBOOK;
import static com.test.conf.OAuth2AuthClientConfig.LOGIN_GOOGLE;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final UserDetailServiceCustom userDetailsService;

    @Autowired
    @Qualifier("googleAuthenticationProcessingFilter")
    private Filter googleSsoFilter;
    @Autowired
    @Qualifier("facebookAuthenticationProcessingFilter")
    private Filter facebookSsoFilter;

    @Autowired
    public SecurityConfiguration(@Lazy UserDetailServiceCustom userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
             .httpBasic().and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(securityContextLogoutSuccessHandler())
                .permitAll().and()
             .authorizeRequests()
                .antMatchers("/", "/users/login", "/users/register", "/users/confirm").permitAll()
                .antMatchers(LOGIN_GOOGLE, LOGIN_FACEBOOK).permitAll()
                .antMatchers("/api").hasRole("USER")
                .antMatchers("/users", "/admin/**").hasRole("ADMIN")
                .antMatchers("/manage/**").hasRole("ADMIN")
                .anyRequest().authenticated().and()
             .csrf()
                .disable()
             .headers()
                .frameOptions().sameOrigin().and()
             .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
             .addFilterBefore(googleSsoFilter, BasicAuthenticationFilter.class)
             .addFilterBefore(facebookSsoFilter, BasicAuthenticationFilter.class);
    }


    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    protected LogoutSuccessHandler securityContextLogoutSuccessHandler() {
        return new SecurityContextLogoutSuccessHandler();
    }

    private static class SecurityContextLogoutSuccessHandler extends SecurityContextLogoutHandler implements LogoutSuccessHandler {
        @Override
        public void onLogoutSuccess(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Authentication authentication) throws IOException, ServletException {
            //TODO: maybe set token as expired
            super.logout(request, response, authentication);
        }
    }
}
