package br.com.tmvinicius.home.hub.domain.usecase.auth;


import br.com.tmvinicius.home.hub.domain.exception.user.InvalidUserException;
import br.com.tmvinicius.home.hub.domain.exception.user.InvalidUserLoginException;
import br.com.tmvinicius.home.hub.domain.model.auth.LoginResult;
import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.model.user.User;
import br.com.tmvinicius.home.hub.domain.port.in.auth.RefreshTokenUseCase;
import br.com.tmvinicius.home.hub.domain.port.out.auth.PasswordEncoder;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import br.com.tmvinicius.home.hub.domain.port.out.user.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    @InjectMocks
    private LoginUseCaseImpl loginUseCase;


    private UUID userID;
    private Email loginEmail;
    private Password loginPassword;
    private Password dataBasePassword;
    private User validUser;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {

        userID = UUID.randomUUID();
        loginEmail = new Email("user@homehub.dev");
        loginPassword = Password.of("Home@123");
        dataBasePassword = Password.fromHash("$2a$10$stored-hash");

        validUser = new User(
                userID,
                loginEmail,
                dataBasePassword
        );

        refreshToken = new RefreshToken(
                UUID.randomUUID(),
                userID,
                "refresh-token",
                Instant.now(),
                false
        );

    }

    @Test
    void shouldLoginValidUserWhenValidCredentials(){
        when(userRepository.findByEmail(loginEmail)).thenReturn(Optional.of(validUser));
        when(passwordEncoder.verify(loginPassword, dataBasePassword)).thenReturn(true);
        when(tokenProvider.generate(validUser)).thenReturn("access-token");
        when(refreshTokenUseCase.createRefreshToken(validUser.getId())).thenReturn(refreshToken);

        LoginResult result = loginUseCase.userLogin(loginEmail, loginPassword);

        assertAll(
                () -> assertEquals("access-token", result.accessToken()),
                () -> assertEquals(refreshToken.getToken(), result.refreshToken())
        );

        verify(userRepository).findByEmail(loginEmail);
        verify(passwordEncoder).verify(loginPassword, dataBasePassword);
        verify(tokenProvider).generate(validUser);
        verify(refreshTokenUseCase).createRefreshToken(userID);

    }

    @Test
    void shouldThrowInvalidUserLoginExceptionWhenUserIsNull(){
        when(userRepository.findByEmail(loginEmail)).thenReturn(Optional.empty());

        assertThrows(
                InvalidUserLoginException.class,
                () -> loginUseCase.userLogin(loginEmail, loginPassword));

        verifyNoInteractions(passwordEncoder, tokenProvider, refreshTokenUseCase);
    }

    @Test
    void shouldRejectLoginWhenPasswordIsInvalid(){
        when(userRepository.findByEmail(loginEmail)).thenReturn(Optional.of(validUser));
        when(passwordEncoder.verify(loginPassword, dataBasePassword)).thenReturn(false);

        assertThrows(
                InvalidUserLoginException.class,
                () -> loginUseCase.userLogin(loginEmail, loginPassword));

        verifyNoInteractions(tokenProvider, refreshTokenUseCase);
    }

    @Test
    void shouldRejectLoginWhenUserIsInactive(){
        User invalidUser = validUser;
        invalidUser.setActive(false);

        when(userRepository.findByEmail(loginEmail)).thenReturn(Optional.of(invalidUser));
        when(passwordEncoder.verify(loginPassword, dataBasePassword)).thenReturn(true);

        assertThrows(
                InvalidUserException.class,
                () -> loginUseCase.userLogin(loginEmail, loginPassword));

        verifyNoInteractions(tokenProvider, refreshTokenUseCase);
    }




}
