package br.com.tmvinicius.home.hub.infrastructure.persistence.repository;

import br.com.tmvinicius.home.hub.infrastructure.persistence.user.UserPersistence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserPersistence, UUID> {

    public UserPersistence findByEmail(String email);
}
