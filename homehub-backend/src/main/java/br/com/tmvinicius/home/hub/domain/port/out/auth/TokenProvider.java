package br.com.tmvinicius.home.hub.domain.port.out.auth;

import br.com.tmvinicius.home.hub.domain.model.user.User;

public interface TokenProvider {

    //gerar token

    String generate(User user);
}
