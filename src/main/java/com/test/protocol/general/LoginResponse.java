package com.test.protocol.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.test.domain.dto.User;

public class LoginResponse extends Response {
    private Long id;

    private String name;

    private String email;

    @JsonProperty("admin")
    private boolean isAdmin;

    public LoginResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.isAdmin = user.isAdmin();
    }
}
