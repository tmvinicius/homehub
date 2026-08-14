package br.com.tmvinicius.home.hub.domain.model.auth;

import br.com.tmvinicius.home.hub.domain.exception.auth.RefreshTokenExpiredException;
import br.com.tmvinicius.home.hub.domain.exception.auth.RefreshTokenRevokedException;

import java.time.Instant;
import java.util.UUID;

public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String token;
    private Instant expiresAt;
    private Boolean revoked;


    public RefreshToken(UUID id, UUID userId, String token, Instant  expiresAt, Boolean revoked){
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public Instant  getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void validateRefreshToken() {

        if(isRevoked()){
            throw new RefreshTokenRevokedException("O token não está mais diponível");
        }

        if (isExpired()){
            throw new RefreshTokenExpiredException("O token expirou");
        }
    }

}
