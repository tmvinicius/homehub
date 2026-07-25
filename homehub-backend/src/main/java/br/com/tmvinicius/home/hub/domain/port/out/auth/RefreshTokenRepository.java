package br.com.tmvinicius.home.hub.domain.port.out.auth;

import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);
}
