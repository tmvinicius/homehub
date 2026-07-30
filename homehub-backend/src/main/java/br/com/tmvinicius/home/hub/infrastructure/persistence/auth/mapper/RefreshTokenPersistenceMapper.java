package br.com.tmvinicius.home.hub.infrastructure.persistence.auth.mapper;

import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;
import br.com.tmvinicius.home.hub.infrastructure.persistence.auth.RefreshTokenPersistence;

public class RefreshTokenPersistenceMapper {


    public RefreshTokenPersistence domainToEntity(RefreshToken refreshToken){
        return new RefreshTokenPersistence(
                refreshToken.getId(),
                refreshToken.getUserId(),
                refreshToken.getToken(),
                refreshToken.getExpiresAt(),
                refreshToken.isRevoked());

    }

    public RefreshToken entityToDomain(RefreshTokenPersistence refreshTokenPersistence){
        return new RefreshToken(
                refreshTokenPersistence.getId(),
                refreshTokenPersistence.getUserId(),
                refreshTokenPersistence.getToken(),
                refreshTokenPersistence.getExpiresAt(),
                refreshTokenPersistence.isRevoked());
    }

}
