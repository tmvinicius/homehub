package br.com.tmvinicius.home.hub.domain.model.user;

import br.com.tmvinicius.home.hub.domain.exception.user.InvalidPasswordException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordTest {
    @Test
    public void shouldCreateValidPassword(){
        assertDoesNotThrow( () -> new Password("AbC1020@$;"));
    }

    @Test
    public void shouldThrowInvalidPasswordExceptionWhenInvalidPassword(){
        assertThrows( InvalidPasswordException.class,
                () -> new Password("AC1234@;"));
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenPasswordIsNull(){
        assertThrows( NullPointerException.class,
                () -> new Password(null));
    }

    @Test
    public void shouldThrowInvalidPasswordExceptionWhenPasswordIsVoid(){
        assertThrows( InvalidPasswordException.class,
                () -> new Password(""));
    }

    @Test
    public void shouldReturnPasswordFromHash(){
        String hash = "$2a$10$ql5cspcF6tHZgWmwR2RTk.75Ez1RLN5v4dJRAWIWrKeFMx1jTv/2e"; //tmV123456@

        Password passwordFromHash = Password.fromHash(hash);

        assertEquals(hash, passwordFromHash.getValue());
    }

    @Test
    public void shouldParseToPassword(){
        String requestPassword = "Abc123@";

        Password password = Password.of(requestPassword);

        assertEquals(requestPassword, password.getValue());

    }


}
