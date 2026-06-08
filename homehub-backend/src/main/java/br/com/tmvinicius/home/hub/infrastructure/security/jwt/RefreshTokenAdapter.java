package br.com.tmvinicius.home.hub.infrastructure.security.jwt;

import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;
import br.com.tmvinicius.home.hub.domain.model.user.User;
import br.com.tmvinicius.home.hub.domain.port.in.auth.RefreshTokenUseCase;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenProvider;
import br.com.tmvinicius.home.hub.domain.port.out.user.UserRepository;
import br.com.tmvinicius.home.hub.infrastructure.persistence.auth.RefreshTokenPersistence;
import br.com.tmvinicius.home.hub.infrastructure.persistence.repository.RefreshTokenRepository;

import java.util.Optional;
import java.util.UUID;

public class RefreshTokenAdapter implements RefreshTokenProvider {

    private final long expiration;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenAdapter(RefreshTokenProperties tokenProperties, RefreshTokenUseCase tokenUseCase, RefreshTokenRepository tokenRepository, UserRepository userRepository){
        this.expiration = tokenProperties.expiration();
        this.refreshTokenRepository = tokenRepository;
        this.refreshTokenUseCase = tokenUseCase;
        this.userRepository = userRepository;
    }


    @Override
    public RefreshToken generate(UUID userId) {

        Optional<User> tokenPersistence = userRepository.findById(userId);


        return null;
    }
}
