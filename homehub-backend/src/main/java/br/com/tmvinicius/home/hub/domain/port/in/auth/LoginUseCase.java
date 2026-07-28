package br.com.tmvinicius.home.hub.domain.port.in.auth;

import br.com.tmvinicius.home.hub.domain.model.auth.LoginResult;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.response.user.UserLoginResponse;

public interface LoginUseCase {

    LoginResult userLogin(Email email, Password password);
}