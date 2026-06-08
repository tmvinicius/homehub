package br.com.tmvinicius.home.hub.domain.port.out.user;

import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findByEmail(Email email);

    Optional<User> findById(UUID id);

}
