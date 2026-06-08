package br.com.tmvinicius.home.hub.domain.port.out.auth;

import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;

import java.util.UUID;

public interface RefreshTokenProvider {

    RefreshToken generate(UUID userId);

}
