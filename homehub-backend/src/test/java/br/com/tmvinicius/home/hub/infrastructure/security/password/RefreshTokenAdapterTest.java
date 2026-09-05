package br.com.tmvinicius.home.hub.infrastructure.security.password;


import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;
import br.com.tmvinicius.home.hub.infrastructure.security.jwt.RefreshTokenAdapter;
import br.com.tmvinicius.home.hub.infrastructure.security.jwt.RefreshTokenProperties;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenAdapterTest {

    private final String SECRET = "test-secret-unit-test-jwt-adapter-0987654321";
    private final long EXPIRATION = 86400000L;
    private final UUID userId = UUID.randomUUID();

    private RefreshTokenAdapter refreshTokenAdapter;
    private RefreshToken token;

    @BeforeEach
    void setUp(){
        RefreshTokenProperties properties = mock(RefreshTokenProperties.class);
        when(properties.expiration()).thenReturn(EXPIRATION);
        refreshTokenAdapter = new RefreshTokenAdapter(properties);

        token = new RefreshToken(UUID.randomUUID(),
                userId,
                "valid-token",
                Instant.now().plusMillis(EXPIRATION),
                false);
    }

    @Test
    void shouldGenerateToken(){
        RefreshToken result = refreshTokenAdapter.generate(userId);

        assertAll(
                () -> assertEquals(userId, result.getUserId()),
                () -> assertNotNull(result.getToken()),
                () -> assertNotNull(result.getId()),
                () -> assertFalse(result.getToken().isBlank()),
                () -> assertFalse(token.isRevoked()),
                () -> assertFalse(token.isExpired())
        );


    }







}
