package br.com.tmvinicius.home.hub.infrastructure;

import br.com.tmvinicius.home.hub.domain.port.in.auth.LoginUseCase;
import br.com.tmvinicius.home.hub.domain.port.in.auth.RefreshTokenUseCase;
import br.com.tmvinicius.home.hub.domain.port.in.auth.VerifyTokenUseCase;
import br.com.tmvinicius.home.hub.domain.port.out.auth.PasswordEncoder;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenGenerator;
import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenRepository;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import br.com.tmvinicius.home.hub.domain.port.out.user.UserRepository;
import br.com.tmvinicius.home.hub.domain.usecase.auth.LoginUseCaseImpl;
import br.com.tmvinicius.home.hub.domain.usecase.auth.RefreshTokenUseCaseImpl;
import br.com.tmvinicius.home.hub.domain.usecase.auth.VerifyTokenUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public LoginUseCase loginUseCase(UserRepository userRepository, TokenProvider tokenProvider, PasswordEncoder passwordEncoder, RefreshTokenUseCase refreshTokenUseCase){
        return new LoginUseCaseImpl(userRepository,tokenProvider,passwordEncoder, refreshTokenUseCase);
    }

    @Bean
    public VerifyTokenUseCase verifyTokenUseCase(TokenProvider tokenProvider){
        return new VerifyTokenUseCaseImpl(tokenProvider);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(RefreshTokenGenerator refreshTokenGenerator, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, TokenProvider tokenProvider){
        return new RefreshTokenUseCaseImpl(refreshTokenGenerator, refreshTokenRepository, userRepository, tokenProvider);
    }

}
