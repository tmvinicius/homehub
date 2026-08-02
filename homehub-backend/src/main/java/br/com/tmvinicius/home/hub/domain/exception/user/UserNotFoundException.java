package br.com.tmvinicius.home.hub.domain.exception.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
