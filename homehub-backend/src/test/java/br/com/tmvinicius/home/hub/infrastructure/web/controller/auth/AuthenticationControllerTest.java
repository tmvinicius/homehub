package br.com.tmvinicius.home.hub.infrastructure.web.controller.auth;

import br.com.tmvinicius.home.hub.domain.exception.auth.TokenInvalidException;
import br.com.tmvinicius.home.hub.domain.model.auth.AuthenticatedUser;
import br.com.tmvinicius.home.hub.domain.model.auth.LoginResult;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.model.user.UserRole;
import br.com.tmvinicius.home.hub.domain.port.in.auth.LoginUseCase;
import br.com.tmvinicius.home.hub.domain.port.in.auth.RefreshTokenUseCase;
import br.com.tmvinicius.home.hub.domain.port.in.auth.VerifyTokenUseCase;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.request.auth.RefreshAccessTokenRequest;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.request.auth.RevokeRefreshTokenRequest;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.request.user.UserLoginRequest;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.response.auth.RefreshAccessTokenResponse;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.response.user.MeResponse;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.response.user.UserLoginResponse;
import br.com.tmvinicius.home.hub.infrastructure.web.mapper.AuthMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

    @Mock
    private LoginUseCase loginUseCase;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private VerifyTokenUseCase verifyTokenUseCase;

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    @InjectMocks
    private AuthenticationController authenticationController;

    private UUID userId;
    private Email email;
    private Password password;
    private UserLoginRequest loginRequest;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        email = new Email("user@homehub.dev");
        password = Password.of("Home@123");

        loginRequest = new UserLoginRequest("user@homehub.dev", "Home@123");

        authenticatedUser = new AuthenticatedUser(
                userId,
                email,
                UserRole.USER
        );
    }

    @Test
    void shouldReturnTokensWhenLoginSucceeds() {
        when(authMapper.toEmail(loginRequest)).thenReturn(email);
        when(authMapper.toPassword(loginRequest)).thenReturn(password);
        when(loginUseCase.userLogin(email, password))
                .thenReturn(new LoginResult("access-token", "refresh-token"));

        ResponseEntity<UserLoginResponse> response = authenticationController.userLogin(loginRequest);

        UserLoginResponse body = response.getBody();

        assertNotNull(body);
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("access-token", body.accessToken()),
                () -> assertEquals("refresh-token", body.refreshToken())
        );

        verify(authMapper).toEmail(loginRequest);
        verify(authMapper).toPassword(loginRequest);
        verify(loginUseCase).userLogin(email, password);
        verifyNoInteractions(verifyTokenUseCase, refreshTokenUseCase);
    }

    @Test
    void shouldReturnOkWhenAccessTokenIsValid() {
        when(verifyTokenUseCase.verify("access-token")).thenReturn(true);

        ResponseEntity<Void> response = authenticationController.userVerify("Bearer access-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(verifyTokenUseCase).verify("access-token");
        verifyNoInteractions(loginUseCase, authMapper, refreshTokenUseCase);
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenAuthorizationHeaderIsMissing() {
        assertThrows(
                TokenInvalidException.class,
                () -> authenticationController.userVerify(null));

        verifyNoInteractions(loginUseCase, authMapper, verifyTokenUseCase, refreshTokenUseCase);
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenAuthorizationHeaderHasNoBearerPrefix() {
        assertThrows(
                TokenInvalidException.class,
                () -> authenticationController.userVerify("access-token"));

        verifyNoInteractions(loginUseCase, authMapper, verifyTokenUseCase, refreshTokenUseCase);
    }

    @Test
    void shouldReturnAuthenticatedUserDataWhenPrincipalIsPresent() {
        ResponseEntity<MeResponse> response = authenticationController.userMe(authenticatedUser);

        MeResponse body = response.getBody();

        assertNotNull(body);
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(userId, body.id()),
                () -> assertEquals("user@homehub.dev", body.email()),
                () -> assertEquals("USER", body.role())
        );

        verifyNoInteractions(loginUseCase, authMapper, verifyTokenUseCase, refreshTokenUseCase);
    }

    @Test
    void shouldReturnNewAccessTokenWhenRefreshTokenIsValid() {
        when(refreshTokenUseCase.refreshAccessToken("refresh-token")).thenReturn("new-access-token");

        ResponseEntity<RefreshAccessTokenResponse> response =
                authenticationController.refreshAccessToken(new RefreshAccessTokenRequest("refresh-token"));

        RefreshAccessTokenResponse body = response.getBody();

        assertNotNull(body);
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("new-access-token", body.accessToken())
        );

        verify(refreshTokenUseCase).refreshAccessToken("refresh-token");
        verifyNoInteractions(loginUseCase, authMapper, verifyTokenUseCase);
    }

    @Test
    void shouldReturnNoContentWhenLogoutSucceeds() {
        ResponseEntity<Void> response =
                authenticationController.logout(new RevokeRefreshTokenRequest("refresh-token"));

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(refreshTokenUseCase).revokeToken("refresh-token");
        verifyNoInteractions(loginUseCase, authMapper, verifyTokenUseCase);
    }

}
