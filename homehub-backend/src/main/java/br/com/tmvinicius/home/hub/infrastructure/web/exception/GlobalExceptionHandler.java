package br.com.tmvinicius.home.hub.infrastructure.web.exception;

import br.com.tmvinicius.home.hub.domain.exception.auth.InvalidRefreshTokenException;
import br.com.tmvinicius.home.hub.domain.exception.auth.RefreshTokenExpiredException;
import br.com.tmvinicius.home.hub.domain.exception.auth.RefreshTokenRevokedException;
import br.com.tmvinicius.home.hub.domain.exception.auth.TokenInvalidException;
import br.com.tmvinicius.home.hub.domain.exception.user.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler{
    //Invalid - Email, Password, User, UserLogin - Ex

    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<Object> handleTokenInvalidEx(TokenInvalidException ex, WebRequest request){
        return buildResponse(ex, "TOKEN_INVALID", HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<Object> handleEmailInvalidEx(InvalidEmailException ex, WebRequest request){
        return buildResponse(ex, "EMAIL_INVALID", HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Object> handlePasswordInvalidEx(InvalidPasswordException ex, WebRequest request){
        return buildResponse(ex, "PASSWORD_INVALID", HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(InvalidUserException.class)
    public ResponseEntity<Object> handleUserInvalidEx(InvalidUserException ex, WebRequest request){
        return buildResponse(ex, "USER_INVALID", HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(InvalidUserLoginException.class)
    public ResponseEntity<Object> handleUserLoginInvalidEx(InvalidUserLoginException ex, WebRequest request){
        return buildResponse(ex, "USER_LOGIN_INVALID", HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<Object> handleRefreshTokenRevokedEx(RefreshTokenRevokedException ex, WebRequest request){
        return buildResponse(ex, "REFRESH_TOKEN_INVALID", HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<Object> handleInvalidRefreshTokenEx(InvalidRefreshTokenException ex, WebRequest request){
        return buildResponse(ex, "REFRESH_TOKEN_INVALID", HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<Object> handleTokenExpiredEx(RefreshTokenExpiredException ex, WebRequest request){
        return buildResponse(ex, "REFRESH_TOKEN_INVALID", HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundEx(UserNotFoundException ex, WebRequest request){
        return buildResponse(ex, "USER_NOT_FOUND", HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericEx(Exception ex, WebRequest request){
        return buildResponse(ex, "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<Object> buildResponse(Exception ex,
                                                 String errorCode,
                                                 HttpStatus status,
                                                 WebRequest request){

        ServletWebRequest servletWebRequest = (ServletWebRequest) request;

        RestError error = RestError.builder()
                .errorCode(errorCode)
                .errorMessage(ex.getMessage())
                .status(status.value())
                .path(servletWebRequest.getRequest().getRequestURI())
                .build();

        return handleExceptionInternal(ex, error, new HttpHeaders(), status, request);
    }



}
