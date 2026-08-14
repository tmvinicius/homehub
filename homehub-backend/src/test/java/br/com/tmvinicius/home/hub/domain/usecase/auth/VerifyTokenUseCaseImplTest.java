package br.com.tmvinicius.home.hub.domain.usecase.auth;

import br.com.tmvinicius.home.hub.domain.exception.auth.TokenInvalidException;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class VerifyTokenUseCaseImplTest {


    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private VerifyTokenUseCaseImpl verifyTokenUseCase;

    @Test
    void shouldReturnTrueWhenTokenProviderAcceptsToken(){
        boolean result = verifyTokenUseCase.verify("valid-token");

        assertTrue(result);
        verify(tokenProvider).parseAndValidate("valid-token");
    }

    @Test
    void shouldPropagateTokenValidationFailure() {
        doThrow(new TokenInvalidException("Token inválido"))
                .when(tokenProvider)
                .parseAndValidate("invalid-token");

        assertThrows(
                TokenInvalidException.class,
                () -> verifyTokenUseCase.verify("invalid-token"));
    }
}
