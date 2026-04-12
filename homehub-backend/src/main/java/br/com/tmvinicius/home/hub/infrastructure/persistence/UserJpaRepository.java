package br.com.tmvinicius.home.hub.infrastructure.persistence;

import br.com.tmvinicius.home.hub.infrastructure.persistence.user.UserPersistence;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserJpaRepository extends CrudRepository<UserPersistence, UUID> {

    public UserPersistence findByEmail(String email);
}
