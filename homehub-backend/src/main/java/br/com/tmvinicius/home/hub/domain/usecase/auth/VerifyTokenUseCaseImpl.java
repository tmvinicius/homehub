package br.com.tmvinicius.home.hub.domain.usecase.auth;

import br.com.tmvinicius.home.hub.domain.exception.auth.TokenInvalidException;
import br.com.tmvinicius.home.hub.domain.port.in.auth.VerifyTokenUseCase;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;

public class VerifyTokenUseCaseImpl implements VerifyTokenUseCase {

    private final TokenProvider tokenProvider;

    public VerifyTokenUseCaseImpl(TokenProvider tokenProvider){
        this.tokenProvider= tokenProvider;
    }

    @Override
    public boolean verify(String token) {

        tokenProvider.parseAndValidate(token);
        return true;
    }
}
