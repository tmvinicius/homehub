package br.com.tmvinicius.home.hub.domain.port.in.auth;

import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;

import java.util.UUID;

public interface RefreshTokenUseCase {

    RefreshToken createRefreshToken(UUID userId);

}
