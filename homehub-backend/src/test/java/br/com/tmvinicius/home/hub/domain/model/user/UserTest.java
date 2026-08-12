package br.com.tmvinicius.home.hub.domain.model.user;

import br.com.tmvinicius.home.hub.domain.exception.user.InvalidUserException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    User userAdmin = new User(UUID.randomUUID(),
            new Email("adm@tmvinicius.com"),
            new Password("AbC123@"));

    @Test
    public void shouldCreateValidUser(){
        assertDoesNotThrow(() -> userAdmin);

    }

    @Test
    public void shouldValidateUserWhenUserIsValid(){
        assertDoesNotThrow(() -> userAdmin.validateUser());

    }

    @Test
    public void shouldThrowInvalidUserExceptionWhenUserIsNotActive(){
        User user = new User(
                UUID.randomUUID(),
                new Email("adm@tmvinicius.com"),
                new Password("AbC123@"),
                UserRole.ADMIN,
                false);

        assertThrows(InvalidUserException.class, user::validateUser);
    }

    @Test
    public void shouldThrowInvalidUserExceptionWhenUserRoleIsNotAdmin(){
        User user = new User(
                UUID.randomUUID(),
                new Email("adm@tmvinicius.com"),
                new Password("AbC123@"),
                UserRole.USER,
                true);

        assertThrows(InvalidUserException.class, () -> user.setActive(true));
    }

    @Test
    public void shouldSetActiveUserWhenUserRoleIsAdmin(){
        User admin = new User(
                UUID.randomUUID(),
                new Email("adm@tmvinicius.com"),
                new Password("AbC123@"),
                UserRole.ADMIN,
                true);

        admin.setActive(false);
        assertFalse(admin.getActive());
    }


}
