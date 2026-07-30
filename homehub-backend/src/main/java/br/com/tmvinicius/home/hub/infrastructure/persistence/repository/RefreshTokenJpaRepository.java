package br.com.tmvinicius.home.hub.infrastructure.persistence.repository;

import br.com.tmvinicius.home.hub.infrastructure.persistence.auth.RefreshTokenPersistence;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends CrudRepository<RefreshTokenPersistence, UUID> {

    Optional<RefreshTokenPersistence> findByToken(String token);
}
