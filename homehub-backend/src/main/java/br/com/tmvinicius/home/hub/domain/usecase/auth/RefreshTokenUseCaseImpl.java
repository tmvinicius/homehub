package br.com.tmvinicius.home.hub.domain.usecase.auth;

import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;
import br.com.tmvinicius.home.hub.domain.port.in.auth.RefreshTokenUseCase;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenProvider;
import br.com.tmvinicius.home.hub.domain.port.out.user.UserRepository;

import java.util.UUID;

public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {


    private final UserRepository userRepository;
    private final RefreshTokenProvider refreshTokenProvider;

    public RefreshTokenUseCaseImpl(UserRepository userRepository, RefreshTokenProvider refreshTokenProvider){
        this.userRepository = userRepository;
        this.refreshTokenProvider = refreshTokenProvider;
    }

    @Override
    public RefreshToken createRefreshToken(UUID userId) {
        //nao precisa tratar se nem passar do login(email + senha) entao é pq nao existe userId, logo erro antes
        return refreshTokenProvider.generate(userId);
    }

    @Override
    public boolean isTokenExpired(RefreshToken token) {
        return token.getRevoked();
    }
}
