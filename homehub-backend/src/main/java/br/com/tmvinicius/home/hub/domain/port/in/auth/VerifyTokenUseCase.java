package br.com.tmvinicius.home.hub.domain.port.in.auth;

public interface VerifyTokenUseCase {

    boolean verify(String token);

}
