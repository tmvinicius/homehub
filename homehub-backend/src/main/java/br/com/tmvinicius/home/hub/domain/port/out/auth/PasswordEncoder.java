package br.com.tmvinicius.home.hub.domain.port.out.auth;

import br.com.tmvinicius.home.hub.domain.model.user.Password;

public interface PasswordEncoder {

    //validar senha
    boolean verify(Password rawPassword, Password encodedPassword);

    Password encode(Password rawPassword);
}
