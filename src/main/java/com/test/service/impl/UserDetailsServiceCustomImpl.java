package com.test.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.test.controller.validation.UserExistException;
import com.test.controller.validation.ValidationException;
import com.test.domain.UserEntity;
import com.test.domain.UserRoleEntity;
import com.test.domain.UserRoleType;
import com.test.domain.VerificationTokenEntity;
import com.test.domain.dto.DTOHelper;
import com.test.domain.dto.User;
import com.test.domain.dto.VerificationToken;
import com.test.domain.repository.UserRepository;
import com.test.domain.repository.UserRoleRepository;
import com.test.domain.repository.VerificationTokenRepository;
import com.test.service.UserDetailServiceCustom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class UserDetailsServiceCustomImpl implements UserDetailServiceCustom {
    private final static Logger LOG = LoggerFactory.getLogger(UserDetailsServiceCustomImpl.class);

    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepo;
    private final MessageSource messages;
    private final UserRepository userRepo;
    private final UserRoleRepository userRoleRepo;

    @Autowired
    public UserDetailsServiceCustomImpl(UserRepository userRepo, UserRoleRepository userRoleRepo,
                                        PasswordEncoder passwordEncoder, VerificationTokenRepository verificationTokenRepo,
                                         MessageSource messages) {
        this.userRepo = userRepo;
        this.userRoleRepo = userRoleRepo;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepo = verificationTokenRepo;
        this.messages = messages;
    }

    @Override
    @Transactional
    public User create(User user) {
        UserEntity userEntity = userRepo.findByEmailIgnoreCase(user.getEmail());
        if (userEntity != null)
            throw new UserExistException();

        userEntity = new UserEntity();
        userEntity.setName(user.getName());
        userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        userEntity.setEmail(user.getEmail());
        userEntity.addRole(userRoleRepo.findByRole(UserRoleType.ROLE_USER));
        userEntity.setEnabled(user.isEnabled());

        return DTOHelper.detach(userRepo.save(userEntity));
    }

    @Override
    public VerificationToken createVerificationToken(User user, String token) {
        Long id = user.getId();
        String email = user.getEmail();

        UserEntity userEntity = findEntityByIdOrEmail(id, email);

        if (userEntity == null) {
            throw new ValidationException("There is no User with " + (id == null ? "email: " + email : "id: " + id));
        }

        VerificationTokenEntity tokenEntity = new VerificationTokenEntity(token, userEntity);
        return DTOHelper.detach(verificationTokenRepo.save(tokenEntity));
    }

    @Override
    public VerificationToken getVerificationToken(String token) {
        VerificationTokenEntity tokenEntity = verificationTokenRepo.findOne(token);
        return tokenEntity != null ? DTOHelper.detach(tokenEntity) : null;
    }

    @Override
    @Transactional
    public void confirm(VerificationToken verificationToken) {
        VerificationTokenEntity tokenEntity = verificationTokenRepo.findOne(verificationToken.getToken());

        if (tokenEntity == null) {
            throw new ValidationException(messages.getMessage("user.confirmation.error.token-invalid", null, Locale.US));
        }

        UserEntity userEntity = tokenEntity.getUser();
        if (userEntity == null) {
            throw new ValidationException(messages.getMessage("user.email.not-found", new String[]{verificationToken.getEmail()}, Locale.US));
        }

        userEntity.setEnabled(true);
        userRepo.save(userEntity);

        tokenEntity.setVerified(true);
        verificationTokenRepo.save(tokenEntity);

        LOG.debug("Registration confirmed for {}({}).", userEntity.getEmail(), userEntity.getEmail());
    }

    @Override
    @Transactional
    public User deleteByIdOrEmail(Long id, String email) {
        UserEntity userEntity = findEntityByIdOrEmail(id, email);

        if (userEntity == null) {
            throw new ValidationException("There is no User with " + (id == null ? "email: " + email : "id: " + id));
        }
        userRepo.delete(userEntity);
        return DTOHelper.detach(userEntity);
    }

    @Override
    public User findByIdOrEmail(Long id, String email) {
        UserEntity userEntity = findEntityByIdOrEmail(id, email);
        return userEntity != null ? DTOHelper.detach(userEntity) : null;
    }

    private UserEntity findEntityByIdOrEmail(Long id, String email) {
        UserEntity userEntity = null;

        if (id != null) {
            userEntity = userRepo.findOne(id);
        } else if (StringUtils.hasText(email)) {
            userEntity = userRepo.findByEmailIgnoreCase(email);
        }

        return userEntity;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepo.findByEmailIgnoreCase(username);
        if (userEntity == null) {
            throw new UsernameNotFoundException(String.format("User %s does not exist!", username));
        }
        return new UserEntityRepositoryCredentialDetails(userEntity);
    }

    private final static class UserEntityRepositoryCredentialDetails extends User implements UserDetails {

        @JsonIgnore
        private final Set<UserRoleEntity> authList;
        @JsonIgnore
        private final String password;

        private UserEntityRepositoryCredentialDetails(UserEntity userEntity) {
            super(DTOHelper.detach(userEntity));
            password = userEntity.getPassword();
            authList = userEntity.getRoles();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authList;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return getName();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if ( !(o instanceof User) ) return false;
            User that = (User) o;
            return Objects.equals(getId(), that.getId()) &&
                    Objects.equals(getEmail(), that.getEmail());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId(), getEmail());
        }
    }

}
