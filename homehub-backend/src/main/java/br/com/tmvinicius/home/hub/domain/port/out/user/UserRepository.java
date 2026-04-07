package br.com.tmvinicius.home.hub.domain.port.out.user;

import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.User;

public interface UserRepository {

    User findByEmail(Email email);

}
