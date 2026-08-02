package br.com.tmvinicius.home.hub.infrastructure.persistence.auth.adapter;

import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenRepository;
import br.com.tmvinicius.home.hub.infrastructure.persistence.auth.RefreshTokenPersistence;
import br.com.tmvinicius.home.hub.infrastructure.persistence.auth.mapper.RefreshTokenPersistenceMapper;
import br.com.tmvinicius.home.hub.infrastructure.persistence.repository.RefreshTokenJpaRepository;

import java.util.Optional;

public class RefreshTokenAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final RefreshTokenPersistenceMapper refreshTokenPersistenceMapper;

    public RefreshTokenAdapter(RefreshTokenJpaRepository refreshTokenJpaRepository, RefreshTokenPersistenceMapper refreshTokenPersistenceMapper) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
        this.refreshTokenPersistenceMapper = refreshTokenPersistenceMapper;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {

        RefreshTokenPersistence tokenPersistence = refreshTokenPersistenceMapper.domainToEntity(refreshToken);
        refreshTokenJpaRepository.save(tokenPersistence);

        return refreshTokenPersistenceMapper.entityToDomain(tokenPersistence);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {

        return refreshTokenJpaRepository.findByToken(token)
                .map(refreshTokenPersistenceMapper::entityToDomain);
    }
}
