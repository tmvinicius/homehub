package br.com.tmvinicius.home.hub.domain.port.in.auth;

import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;

public interface LoginUseCase {

    String userLogin(Email email, Password password);

}
