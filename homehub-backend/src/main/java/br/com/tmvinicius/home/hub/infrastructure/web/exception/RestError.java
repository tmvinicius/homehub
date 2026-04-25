package br.com.tmvinicius.home.hub.infrastructure.web.exception;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class RestError {

    private final String errorCode;
    private final String errorMessage;
    private final int status;
    private final String path;

    public RestError(String errorCode, String errorMessage, int status, String path){
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = status;
        this.path = path;
    }



}
