package br.com.tmvinicius.home.hub.domain.model.auth;

import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.UserRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthenticatedUserTest {

    @Test
    void shouldAuthenticatedUserIdentity() {
        UUID userId = UUID.randomUUID();
        Email email = new Email("user@homehub.dev");

        AuthenticatedUser authenticatedUser =
                new AuthenticatedUser(userId, email, UserRole.USER);

        assertAll(
                () -> assertEquals(userId, authenticatedUser.getUserId()),
                () -> assertEquals(email, authenticatedUser.getEmail()),
                () -> assertEquals(UserRole.USER, authenticatedUser.getRole())
        );
    }
}
