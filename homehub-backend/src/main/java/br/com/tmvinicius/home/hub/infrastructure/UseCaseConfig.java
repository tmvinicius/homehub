package br.com.tmvinicius.home.hub.infrastructure;

import br.com.tmvinicius.home.hub.domain.port.in.auth.LoginUseCase;
import br.com.tmvinicius.home.hub.domain.port.in.auth.VerifyTokenUseCase;
import br.com.tmvinicius.home.hub.domain.port.out.auth.PasswordEncoder;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import br.com.tmvinicius.home.hub.domain.port.out.user.UserRepository;
import br.com.tmvinicius.home.hub.domain.usecase.auth.LoginUseCaseImpl;
import br.com.tmvinicius.home.hub.domain.usecase.auth.VerifyTokenUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public LoginUseCase loginUseCase(UserRepository userRepository,
                                     TokenProvider tokenProvider,
                                     PasswordEncoder passwordEncoder){
        return new LoginUseCaseImpl(userRepository,tokenProvider,passwordEncoder);
    }

    @Bean
    public VerifyTokenUseCase verifyTokenUseCase(TokenProvider tokenProvider){
        return new VerifyTokenUseCaseImpl(tokenProvider);
    }


}
