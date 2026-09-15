package br.com.tmvinicius.home.hub.domain.exception.auth;

public class TokenInvalidException extends RuntimeException {
    public TokenInvalidException(String message) {
        super(message);
    }
}
