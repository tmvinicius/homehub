package br.com.tmvinicius.home.hub.domain.usecase.auth;

import br.com.tmvinicius.home.hub.domain.exception.InvalidUserLoginException;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.model.user.User;
import br.com.tmvinicius.home.hub.domain.port.in.auth.LoginUseCase;
import br.com.tmvinicius.home.hub.domain.port.out.auth.PasswordEncoder;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import br.com.tmvinicius.home.hub.domain.port.out.user.UserRepository;

public class LoginUseCaseImpl implements LoginUseCase {

    UserRepository userRepository;
    TokenProvider tokenProvider;
    PasswordEncoder passwordEncoder;

    @Override
    public String userLogin(Email email, Password password) {

        User user = userRepository.findByEmail(email);

        if(user == null || !passwordEncoder.verify(password, user.getPassword())){
            throw new InvalidUserLoginException("Credenciais inválidas!");
        }
        user.validateUser();

        String token = tokenProvider.generate(user);
        return token;
    }
}
