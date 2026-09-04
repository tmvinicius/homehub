package br.com.tmvinicius.home.hub.infrastructure.security.password;


import br.com.tmvinicius.home.hub.domain.exception.auth.TokenInvalidException;
import br.com.tmvinicius.home.hub.domain.model.auth.AuthenticatedUser;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.model.user.User;
import br.com.tmvinicius.home.hub.domain.model.user.UserRole;
import br.com.tmvinicius.home.hub.infrastructure.security.jwt.JwtProperties;
import br.com.tmvinicius.home.hub.infrastructure.security.jwt.JwtTokenAdapter;


import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;



@ExtendWith(MockitoExtension.class)
public class JwtTokenAdapterTest {

    private final String SECRET = "test-secret-unit-test-jwt-adapter-0987654321";
    private final long EXPIRATION = 86400000L;

    JwtTokenAdapter jwtTokenAdapter;

    private User user;

    @BeforeEach
    void setUp(){
        JwtProperties properties = mock(JwtProperties.class);
        when(properties.secret()).thenReturn(SECRET);
        when(properties.expiration()).thenReturn(EXPIRATION);

        jwtTokenAdapter = new JwtTokenAdapter(properties);

        user = new User(UUID.randomUUID(),
                new Email("test@homehub.com"),
                new Password("Abc23@1a"),
                UserRole.USER,
                true);

    }

    @Test
    void shouldGenerateAndParseTokenWhenUserIsValid(){
        String token = jwtTokenAdapter.generate(user);
        AuthenticatedUser authenticatedUser = jwtTokenAdapter.parseAndValidate(token);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertAll(
                () -> assertEquals(user.getId(), authenticatedUser.getUserId()),
                () -> assertEquals(user.getEmail(), authenticatedUser.getEmail()),
                () -> assertEquals(user.getRole(), authenticatedUser.getRole())
        );
    }

    @Test
    void shouldRejectTamperedToken(){
        String token = jwtTokenAdapter.generate(user);
        String tamperedToken = token + "invalid";

        assertThrows(TokenInvalidException.class,
                () -> jwtTokenAdapter.parseAndValidate(tamperedToken));
    }

    @Test
    void shouldRejectExpiredToken(){
        JwtProperties expiredProperties = mock(JwtProperties.class);
        when(expiredProperties.secret()).thenReturn(SECRET);
        when(expiredProperties.expiration()).thenReturn(-100L);
        JwtTokenAdapter adapter = new JwtTokenAdapter(expiredProperties);

        String token = adapter.generate(user);

        assertThrows(TokenInvalidException.class,
                ()-> adapter.parseAndValidate(token));
    }







}
