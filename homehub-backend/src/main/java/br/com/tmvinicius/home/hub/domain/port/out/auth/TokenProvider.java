package br.com.tmvinicius.home.hub.domain.port.out.auth;

import br.com.tmvinicius.home.hub.domain.model.auth.AuthenticatedUser;
import br.com.tmvinicius.home.hub.domain.model.user.User;

public interface TokenProvider {

    String generate(User user);

    AuthenticatedUser parseAndValidate(String token);

}
