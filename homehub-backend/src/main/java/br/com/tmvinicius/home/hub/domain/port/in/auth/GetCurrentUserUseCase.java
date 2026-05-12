package br.com.tmvinicius.home.hub.domain.port.in.auth;

import br.com.tmvinicius.home.hub.domain.model.auth.AuthenticatedUser;

public interface GetCurrentUserUseCase {

     AuthenticatedUser getCurrentUser(String token);

}
