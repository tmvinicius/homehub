package br.com.tmvinicius.home.hub.domain.usecase.auth;

import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;
import br.com.tmvinicius.home.hub.domain.port.in.auth.RefreshTokenUseCase;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenGenerator;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenRepository;

import java.util.UUID;

public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {


    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenUseCaseImpl(RefreshTokenGenerator refreshTokenGenerator, RefreshTokenRepository refreshTokenRepository){
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public RefreshToken createRefreshToken(UUID userId) {

        //nao precisa tratar, se nem passar do login(email + senha) entao é pq nao existe userId, logo retorna erro antes
        RefreshToken refreshToken = refreshTokenGenerator.generate(userId);

        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

}
