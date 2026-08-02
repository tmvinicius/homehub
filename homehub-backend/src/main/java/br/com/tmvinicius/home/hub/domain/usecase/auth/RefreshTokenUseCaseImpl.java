package br.com.tmvinicius.home.hub.domain.usecase.auth;

import br.com.tmvinicius.home.hub.domain.exception.auth.InvalidRefreshTokenException;
import br.com.tmvinicius.home.hub.domain.exception.auth.RefreshTokenExpiredException;
import br.com.tmvinicius.home.hub.domain.exception.auth.RefreshTokenRevokedException;
import br.com.tmvinicius.home.hub.domain.exception.user.UserNotFoundException;
import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;
import br.com.tmvinicius.home.hub.domain.model.user.User;
import br.com.tmvinicius.home.hub.domain.port.in.auth.RefreshTokenUseCase;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenGenerator;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenRepository;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import br.com.tmvinicius.home.hub.domain.port.out.user.UserRepository;

import java.util.Optional;
import java.util.UUID;

public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {


    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    public RefreshTokenUseCaseImpl(RefreshTokenGenerator refreshTokenGenerator, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, TokenProvider tokenProvider){
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public RefreshToken createRefreshToken(UUID userId) {

        //nao precisa tratar, se nem passar do login(email + senha) entao é pq nao existe userId, logo retorna erro antes
        RefreshToken refreshToken = refreshTokenGenerator.generate(userId);

        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    @Override
    public String refreshAccessToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh Token inválido"));

        if(refreshToken.isRevoked()){
            throw new RefreshTokenRevokedException("O token não está mais diponível");
        }

        if (refreshToken.isExpired()){
            throw new RefreshTokenExpiredException("O token expirou");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        user.validateUser();

        return tokenProvider.generate(user);
    }

}
