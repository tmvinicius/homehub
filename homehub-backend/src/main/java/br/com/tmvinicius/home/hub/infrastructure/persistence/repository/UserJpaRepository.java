package br.com.tmvinicius.home.hub.infrastructure.persistence.repository;

import br.com.tmvinicius.home.hub.infrastructure.persistence.user.UserPersistence;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserJpaRepository extends CrudRepository<UserPersistence, UUID> {

    public UserPersistence findByEmail(String email);
}
