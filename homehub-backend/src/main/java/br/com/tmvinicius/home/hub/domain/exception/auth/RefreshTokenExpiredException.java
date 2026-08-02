package br.com.tmvinicius.home.hub.domain.exception.auth;

public class RefreshTokenExpiredException extends RuntimeException {
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
