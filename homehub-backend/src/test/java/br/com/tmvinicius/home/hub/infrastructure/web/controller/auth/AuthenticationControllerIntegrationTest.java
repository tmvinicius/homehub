package br.com.tmvinicius.home.hub.infrastructure.web.controller.auth;

import br.com.tmvinicius.home.hub.domain.exception.auth.RefreshTokenExpiredException;
import br.com.tmvinicius.home.hub.domain.exception.user.InvalidUserLoginException;
import br.com.tmvinicius.home.hub.domain.model.auth.AuthenticatedUser;
import br.com.tmvinicius.home.hub.domain.model.auth.LoginResult;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.UserRole;
import br.com.tmvinicius.home.hub.domain.port.in.auth.LoginUseCase;
import br.com.tmvinicius.home.hub.domain.port.in.auth.RefreshTokenUseCase;
import br.com.tmvinicius.home.hub.domain.port.in.auth.VerifyTokenUseCase;
import br.com.tmvinicius.home.hub.infrastructure.web.exception.GlobalExceptionHandler;
import br.com.tmvinicius.home.hub.infrastructure.web.mapper.AuthMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerIntegrationTest {

    private LoginUseCase loginUseCase;
    private VerifyTokenUseCase verifyTokenUseCase;
    private RefreshTokenUseCase refreshTokenUseCase;

    private UUID userId;
    private AuthenticatedUser authenticatedUser;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        loginUseCase = mock(LoginUseCase.class);
        verifyTokenUseCase = mock(VerifyTokenUseCase.class);
        refreshTokenUseCase = mock(RefreshTokenUseCase.class);

        userId = UUID.randomUUID();
        authenticatedUser = new AuthenticatedUser(
                userId,
                new Email("user@homehub.dev"),
                UserRole.USER
        );

        AuthenticationController authenticationController = new AuthenticationController(
                loginUseCase,
                new AuthMapper(),
                verifyTokenUseCase,
                refreshTokenUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnTokensWhenLoginRequestIsValid() throws Exception {
        when(loginUseCase.userLogin(any(), any()))
                .thenReturn(new LoginResult("access-token", "refresh-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user@homehub.dev", "password": "Home@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void shouldReturnBadRequestWhenLoginEmailIsMalformed() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user-homehub.dev", "password": "Home@123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("EMAIL_INVALID"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));

        verifyNoInteractions(loginUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenLoginPasswordDoesNotMatchPolicy() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user@homehub.dev", "password": "homehub"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_INVALID"));

        verifyNoInteractions(loginUseCase);
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginCredentialsAreInvalid() throws Exception {
        when(loginUseCase.userLogin(any(), any()))
                .thenThrow(new InvalidUserLoginException("Credenciais invalidas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user@homehub.dev", "password": "Home@123"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("USER_LOGIN_INVALID"))
                .andExpect(jsonPath("$.errorMessage").value("Credenciais invalidas"));
    }

    @Test
    void shouldReturnOkWhenVerifyReceivesBearerToken() throws Exception {
        when(verifyTokenUseCase.verify("access-token")).thenReturn(true);

        mockMvc.perform(get("/api/auth/verify")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk());

        verify(verifyTokenUseCase).verify("access-token");
    }

    @Test
    void shouldReturnUnauthorizedWhenVerifyReceivesNoAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/auth/verify"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("TOKEN_INVALID"));

        verifyNoInteractions(verifyTokenUseCase);
    }

    @Test
    void shouldReturnAuthenticatedUserDataWhenPrincipalIsPresent() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authenticatedUser, null, List.of()));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("user@homehub.dev"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldReturnNewAccessTokenWhenRefreshTokenIsValid() throws Exception {
        when(refreshTokenUseCase.refreshAccessToken("refresh-token")).thenReturn("new-access-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token": "refresh-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenIsExpired() throws Exception {
        when(refreshTokenUseCase.refreshAccessToken("refresh-token"))
                .thenThrow(new RefreshTokenExpiredException("Refresh token expirado"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token": "refresh-token"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("REFRESH_TOKEN_INVALID"));
    }

    @Test
    void shouldReturnNoContentWhenLogoutRevokesRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token": "refresh-token"}
                                """))
                .andExpect(status().isNoContent());

        verify(refreshTokenUseCase).revokeToken("refresh-token");
    }

}
