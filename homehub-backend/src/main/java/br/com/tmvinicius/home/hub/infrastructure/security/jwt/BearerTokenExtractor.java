package br.com.tmvinicius.home.hub.infrastructure.security.jwt;

import br.com.tmvinicius.home.hub.domain.exception.auth.TokenInvalidException;

public class BearerTokenExtractor {

    public static String extractBearerToken(String token){

        if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
            throw new TokenInvalidException("Authorization header invalido");
        }

        return token.replace("Bearer ", "");

    }

}
