package br.com.tmvinicius.home.hub.infrastructure.security;

import br.com.tmvinicius.home.hub.domain.model.auth.AuthenticatedUser;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.UserRole;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import br.com.tmvinicius.home.hub.infrastructure.security.filter.JwtFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = WebSecurityConfigIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)

@AutoConfigureMockMvc
class WebSecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        reset(tokenProvider);

        when(tokenProvider.parseAndValidate("admin-token"))
                .thenReturn(authenticatedUser(UserRole.ADMIN));
        when(tokenProvider.parseAndValidate("user-token"))
                .thenReturn(authenticatedUser(UserRole.USER));
    }

    @Test
    void shouldAllowPublicAuthenticationEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAdminToAccessUsersEndpoints() throws Exception {
        mockMvc.perform(get("/api/users/test")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyRegularUserFromUsersEndpoints() throws Exception {
        mockMvc.perform(get("/api/users/test")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowRegularUserToAccessServicesEndpoints() throws Exception {
        mockMvc.perform(get("/api/services/test")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForAnyOtherEndpoint() throws Exception {
        mockMvc.perform(get("/private"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/private")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk());
    }

    private AuthenticatedUser authenticatedUser(UserRole role) {
        return new AuthenticatedUser(
                UUID.randomUUID(),
                new Email(role.name().toLowerCase() + "@homehub.local"),
                role
        );
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(WebSecurityConfig.class)
    static class TestApplication {

        @Bean
        TokenProvider tokenProvider() {
            return mock(TokenProvider.class);
        }

        @Bean
        JwtFilter jwtFilter(TokenProvider tokenProvider) {
            return new JwtFilter(tokenProvider);
        }

        @Bean
        TestController testController() {
            return new TestController();
        }
    }

    @RestController
    static class TestController {

        @PostMapping("/api/auth/login")
        ResponseEntity<Void> login() {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/api/users/test")
        ResponseEntity<Void> users() {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/api/services/test")
        ResponseEntity<Void> services() {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/private")
        ResponseEntity<Void> privateEndpoint() {
            return ResponseEntity.ok().build();
        }
    }
}
