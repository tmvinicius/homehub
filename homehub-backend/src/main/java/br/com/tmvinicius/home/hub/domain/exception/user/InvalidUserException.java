package br.com.tmvinicius.home.hub.domain.exception.user;

public class InvalidUserException extends RuntimeException {
    public InvalidUserException(String message) {
        super(message);
    }
}
