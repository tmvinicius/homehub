package br.com.tmvinicius.home.hub.domain.port.out.auth;

import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;

import java.util.UUID;

public interface RefreshTokenGenerator {

    RefreshToken generate(UUID userId);

}
