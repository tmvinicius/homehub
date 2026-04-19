package br.com.tmvinicius.home.hub.domain.exception;

public class InvalidUserLoginException extends RuntimeException {
    public InvalidUserLoginException(String message) {
        super(message);
    }
}
