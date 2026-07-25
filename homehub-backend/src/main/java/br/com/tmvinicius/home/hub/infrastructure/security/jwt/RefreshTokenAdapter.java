package br.com.tmvinicius.home.hub.infrastructure.security.jwt;

import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenGenerator;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public class RefreshTokenAdapter implements RefreshTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final long expiration;

    public RefreshTokenAdapter(RefreshTokenProperties properties) {
        this.expiration = properties.expiration();
    }

    @Override
    public RefreshToken generate(UUID userId) {
        return new RefreshToken(
                UUID.randomUUID(),
                userId,
                generateTokenValue(),
                Instant.now().plusMillis(expiration),
                false
        );
    }

    private String generateTokenValue() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
