package br.com.tmvinicius.home.hub.domain.model.auth;

import br.com.tmvinicius.home.hub.domain.exception.auth.RefreshTokenExpiredException;
import br.com.tmvinicius.home.hub.domain.exception.auth.RefreshTokenRevokedException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class RefreshTokenTest {

    @Test
    public void shouldCreateValidToken(){
        assertDoesNotThrow(() -> new RefreshToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RefreshToken",
                Instant.now(),
                Boolean.FALSE));

    }

    @Test
    public void shouldThrowRefreshTokenRevokedExceptionWhenTokenIsRevoked(){
        RefreshToken token = new RefreshToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RefreshToken",
                Instant.now(),
                Boolean.TRUE);

        assertThrows(RefreshTokenRevokedException.class, token::validateRefreshToken);

    }

    @Test
    public void shouldThrowRefreshTokenExpiredExceptionWhenTokenIsExpired(){
        RefreshToken token = new RefreshToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RefreshToken",
                Instant.EPOCH,
                Boolean.FALSE);

        assertThrows(RefreshTokenExpiredException.class, token::validateRefreshToken);

    }

    @Test
    public void shouldRevokeRefreshToken() {
        RefreshToken token = new RefreshToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RefreshToken",
                Instant.now(),
                false);
        token.revoke();
        assertTrue(token.isRevoked());
    }

    }
