package com.test.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.test.controller.validation.IdCheckGroup;
import com.test.controller.validation.RegistrationCheckGroup;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class User implements Serializable {
    @Min(value = 0, groups = IdCheckGroup.class)
    @Max(value = Long.MAX_VALUE, groups = IdCheckGroup.class)
    private Long id;

    @NotBlank(groups = RegistrationCheckGroup.class)
    @Size(max = 50)
    private String name;

    @NotBlank(groups = RegistrationCheckGroup.class)
    @Size(max = 256)
    private String password;

    @NotBlank(groups = RegistrationCheckGroup.class)
    @Email(groups = RegistrationCheckGroup.class)
    private String email;

    @JsonProperty("enabled")
    private boolean isEnabled;

    @JsonProperty("admin")
    private boolean isAdmin;

    public User() {
    }

    protected User(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.isEnabled = user.isEnabled();
        this.isAdmin = user.isAdmin();
    }

    public User(String name, String password, String email, boolean isEnabled, boolean isAdmin) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.isEnabled = isEnabled;
        this.isAdmin = isAdmin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
