package br.com.tmvinicius.home.hub.domain.integration.auth;

import br.com.tmvinicius.home.hub.domain.exception.auth.RefreshTokenRevokedException;
import br.com.tmvinicius.home.hub.domain.model.auth.LoginResult;
import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.model.user.User;
import br.com.tmvinicius.home.hub.domain.model.user.UserRole;
import br.com.tmvinicius.home.hub.domain.port.out.auth.PasswordEncoder;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenGenerator;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenRepository;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import br.com.tmvinicius.home.hub.domain.port.out.user.UserRepository;
import br.com.tmvinicius.home.hub.domain.usecase.auth.LoginUseCaseImpl;
import br.com.tmvinicius.home.hub.domain.usecase.auth.RefreshTokenUseCaseImpl;
import br.com.tmvinicius.home.hub.domain.usecase.auth.VerifyTokenUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationFlowIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private LoginUseCaseImpl loginUseCase;
    private RefreshTokenUseCaseImpl refreshTokenUseCase;
    private VerifyTokenUseCaseImpl verifyTokenUseCase;

    private UUID userId;
    private Email email;
    private Password rawPassword;
    private Password storedPassword;
    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        refreshTokenUseCase = new RefreshTokenUseCaseImpl(
                refreshTokenGenerator,
                refreshTokenRepository,
                userRepository,
                tokenProvider
        );

        loginUseCase = new LoginUseCaseImpl(
                userRepository,
                tokenProvider,
                passwordEncoder,
                refreshTokenUseCase
        );

        verifyTokenUseCase = new VerifyTokenUseCaseImpl(tokenProvider);

        userId = UUID.randomUUID();
        email = new Email("user@homehub.dev");
        rawPassword = Password.of("Home@123");
        storedPassword = Password.fromHash("$2a$10$stored-hash");

        user = new User(
                userId,
                email,
                storedPassword,
                UserRole.USER,
                true
        );

        refreshToken = new RefreshToken(
                UUID.randomUUID(),
                userId,
                "refresh-token",
                Instant.now().plusSeconds(300),
                false
        );
    }

    @Test
    void shouldCompleteAuthenticationSessionLifecycle() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.verify(rawPassword, storedPassword)).thenReturn(true);
        when(refreshTokenGenerator.generate(userId)).thenReturn(refreshToken);
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));
        when(tokenProvider.generate(user)).thenReturn("access-token-1", "access-token-2");

        LoginResult loginResult = loginUseCase.userLogin(email, rawPassword);

        assertAll(
                () -> assertEquals("access-token-1", loginResult.accessToken()),
                () -> assertEquals("refresh-token", loginResult.refreshToken())
        );

        assertTrue(verifyTokenUseCase.verify(loginResult.accessToken()));

        String refreshedAccessToken =
                refreshTokenUseCase.refreshAccessToken(loginResult.refreshToken());

        assertEquals("access-token-2", refreshedAccessToken);

        refreshTokenUseCase.revokeToken(loginResult.refreshToken());

        assertTrue(refreshToken.isRevoked());

        assertThrows(
                RefreshTokenRevokedException.class,
                () -> refreshTokenUseCase.refreshAccessToken(loginResult.refreshToken())
        );

        verify(refreshTokenRepository, times(2)).save(refreshToken);
        verify(tokenProvider, times(2)).generate(user);
        verify(tokenProvider).parseAndValidate("access-token-1");
    }
}
