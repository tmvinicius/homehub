package br.com.tmvinicius.home.hub.domain.usecase.auth;

import br.com.tmvinicius.home.hub.domain.model.auth.AuthenticatedUser;
import br.com.tmvinicius.home.hub.domain.port.in.auth.GetCurrentUserUseCase;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;

public class GetCurrentUserUseCaseImpl implements GetCurrentUserUseCase {


    private final TokenProvider tokenProvider;

    public GetCurrentUserUseCaseImpl(TokenProvider tokenProvider){
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthenticatedUser getCurrentUser(String token) {
        return tokenProvider.parseAndValidate(token);
    }
}
