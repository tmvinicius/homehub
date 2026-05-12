package br.com.tmvinicius.home.hub.infrastructure.web.dto.response.user;

import java.util.UUID;

public record MeResponse(UUID id,
                         String email,
                         String role)
{ }
