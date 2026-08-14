package br.com.tmvinicius.home.hub.domain.model.user;

import br.com.tmvinicius.home.hub.domain.exception.user.InvalidEmailException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EmailTest  {


    @Test
    void shouldCreateValidEmail(){
        assertDoesNotThrow(() -> new Email("adm@tmvinicius.com"));
    }

    @Test
    void shouldThrowInvalidEmailExceptionWhenEmailIsInvalid(){
        assertThrows(InvalidEmailException.class,
                () -> new Email("test@;//"));
    }

    @Test
    void shouldThrowNullPointerExceptionWhenEmailIsNull(){
        assertThrows(NullPointerException.class,
                () -> new Email(null));
    }

    @Test
    void shouldThrowInvalidEmailExceptionWhenEmailIsVoid(){
        assertThrows(InvalidEmailException.class,
                () -> new Email(""));
    }
}
