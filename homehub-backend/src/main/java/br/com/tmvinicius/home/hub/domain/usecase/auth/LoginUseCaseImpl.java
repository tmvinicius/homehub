package br.com.tmvinicius.home.hub.domain.usecase.auth;

import br.com.tmvinicius.home.hub.domain.exception.user.InvalidUserLoginException;
import br.com.tmvinicius.home.hub.domain.model.auth.LoginResult;
import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.model.user.User;
import br.com.tmvinicius.home.hub.domain.port.in.auth.LoginUseCase;
import br.com.tmvinicius.home.hub.domain.port.in.auth.RefreshTokenUseCase;
import br.com.tmvinicius.home.hub.domain.port.out.auth.PasswordEncoder;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import br.com.tmvinicius.home.hub.domain.port.out.user.UserRepository;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.response.user.UserLoginResponse;

public class LoginUseCaseImpl implements LoginUseCase {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public LoginUseCaseImpl(UserRepository userRepository, TokenProvider tokenProvider, PasswordEncoder passwordEncoder, RefreshTokenUseCase refreshTokenUseCase) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @Override
    public LoginResult userLogin(Email email, Password password) {

        User user = userRepository.findByEmail(email)
                .filter(userVerify -> passwordEncoder.verify(password, userVerify.getPassword()))
                .orElseThrow(() -> new InvalidUserLoginException("Credenciais invalidas!"));

        user.validateUser();

        String accessToken = tokenProvider.generate(user);

        RefreshToken refreshToken = refreshTokenUseCase.createRefreshToken(user.getId());

        return new LoginResult(accessToken, refreshToken.getToken());

    }
}
