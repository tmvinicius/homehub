package br.com.tmvinicius.home.hub.infrastructure.security.password;

import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.port.out.auth.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class BCryptPasswordAdapter implements PasswordEncoder {

    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordAdapter(BCryptPasswordEncoder encoder){
        this.encoder = encoder;
    }

    @Override
    public boolean verify(Password rawPassword, Password encodedPassword) {
        return encoder.matches(rawPassword.getValue(),encodedPassword.getValue());
    }

    @Override
    public Password encode(Password rawPassword) {
        String encodedPassword = encoder.encode(rawPassword.getValue());
        return new Password (encodedPassword);
    }
}
