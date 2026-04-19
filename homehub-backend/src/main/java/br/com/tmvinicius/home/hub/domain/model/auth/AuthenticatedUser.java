package br.com.tmvinicius.home.hub.domain.model.auth;

import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.UserRole;

import java.util.UUID;

public class AuthenticatedUser {

    private UUID userId;
    private Email email;
    private UserRole role;

    public AuthenticatedUser(UUID userId, Email email, UserRole role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public Email getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }
}
