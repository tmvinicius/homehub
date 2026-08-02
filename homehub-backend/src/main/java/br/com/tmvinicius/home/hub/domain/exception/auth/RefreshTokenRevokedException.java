package br.com.tmvinicius.home.hub.domain.exception.auth;

public class RefreshTokenRevokedException extends RuntimeException {
    public RefreshTokenRevokedException(String message) {
        super(message);
    }
}
