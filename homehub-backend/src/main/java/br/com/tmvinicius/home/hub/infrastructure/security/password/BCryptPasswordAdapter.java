package br.com.tmvinicius.home.hub.infrastructure.security.password;

import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.port.out.auth.PasswordEncoder;

public class BCryptPasswordAdapter implements PasswordEncoder {

    @Override
    public Boolean verify(Password rawPassword, Password encodedPassword) {
        return null;
    }
}
