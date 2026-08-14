package br.com.tmvinicius.home.hub.domain.usecase.auth;


import br.com.tmvinicius.home.hub.domain.exception.auth.InvalidRefreshTokenException;
import br.com.tmvinicius.home.hub.domain.exception.auth.RefreshTokenExpiredException;
import br.com.tmvinicius.home.hub.domain.exception.user.InvalidUserException;
import br.com.tmvinicius.home.hub.domain.exception.user.UserNotFoundException;
import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.model.user.User;
import br.com.tmvinicius.home.hub.domain.model.user.UserRole;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenGenerator;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenRepository;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import br.com.tmvinicius.home.hub.domain.port.out.user.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.*;
import org.mockito.Mock;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RefreshTokenUseCaseImplTest {

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private RefreshTokenUseCaseImpl refreshTokenUseCase;


    private UUID userId;
    private String token;
    private RefreshToken refreshToken;
    private User user;

    @BeforeEach
    void setUp(){
        userId = UUID.randomUUID();
        token = "refresh-token";

        refreshToken = new RefreshToken(
                userId,
                UUID.randomUUID(),
                token,
                Instant.now().plusSeconds(300),
                false
        );

        user = new User(
                userId,
                new Email("user@homehub.dev"),
                Password.fromHash("$2a$10$stored-hash")
        );
    }




    @Test
    void shouldGenerateRefreshTokenAndReturnToken(){
        when(refreshTokenGenerator.generate(userId)).thenReturn(refreshToken);
        when(refreshTokenRepository.save(refreshToken)).thenReturn(refreshToken);

        RefreshToken result = refreshTokenUseCase.createRefreshToken(userId);

        assertSame(refreshToken, result);

        verify(refreshTokenGenerator).generate(userId);
        verify(refreshTokenRepository).save(refreshToken);
        verifyNoInteractions(userRepository, tokenProvider);
    }

    @Test
    void shouldGenerateAccessTokenForValidRefreshToken(){
        when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(refreshToken.getUserId())).thenReturn(Optional.of(user));
        when(tokenProvider.generate(user)).thenReturn("new-access-token");

        String result = refreshTokenUseCase.refreshAccessToken(refreshToken.getToken());

        assertEquals("new-access-token", result);

        verify(tokenProvider).generate(user);
        verifyNoInteractions(refreshTokenGenerator);
    }

    @Test
    void shouldThrowInvalidRefreshTokenExceptionWhenUnknownRefreshToken(){
        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        assertThrows(
                InvalidRefreshTokenException.class,
                ()-> refreshTokenUseCase.refreshAccessToken("unknown-token"));

        verifyNoInteractions(userRepository, tokenProvider, refreshTokenGenerator);
    }

    @Test
    void shouldThrowRefreshTokenExpiredExceptionWhenExpiredRefreshToken(){
        refreshToken = new RefreshToken(
                userId,
                UUID.randomUUID(),
                token,
                Instant.now().minusSeconds(300),
                false
        );

        when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));

        assertThrows(RefreshTokenExpiredException.class,
                () -> refreshTokenUseCase.refreshAccessToken(refreshToken.getToken()));

        verifyNoInteractions(userRepository, tokenProvider, refreshTokenGenerator);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist(){
        when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(refreshToken.getUserId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> refreshTokenUseCase.refreshAccessToken(refreshToken.getToken()));

        verifyNoInteractions(tokenProvider, refreshTokenGenerator);
    }

    @Test
    void shouldThrowInvalidUserExceptionWhenUserIsInactive(){
        user = new User(
                userId,
                new Email("user@homehub.dev"),
                Password.fromHash("$2a$10$stored-hash"),
                UserRole.ADMIN,
                false
        );

        when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(refreshToken.getUserId())).thenReturn(Optional.of(user));

        assertThrows(InvalidUserException.class,
                () -> refreshTokenUseCase.refreshAccessToken(refreshToken.getToken()));

        verifyNoInteractions(tokenProvider, refreshTokenGenerator);
    }

    @Test
    void shouldRevokeAndPersistRefreshToken(){
        when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));

        refreshTokenUseCase.revokeToken(refreshToken.getToken());

        assertTrue(refreshToken.isRevoked());

        verify(refreshTokenRepository).save(refreshToken);
        verifyNoInteractions(userRepository, tokenProvider, refreshTokenGenerator);
    }

    @Test
    void shouldThrowInvalidRefreshTokenExceptionWhenTokenDoesNotExist(){
        when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class,
                () ->  refreshTokenUseCase.revokeToken(refreshToken.getToken()));

        verify(refreshTokenRepository, never()).save(any());
        verifyNoInteractions(userRepository, tokenProvider, refreshTokenGenerator);
    }











}
