package br.com.tmvinicius.home.hub.infrastructure.persistence.repository;

import br.com.tmvinicius.home.hub.infrastructure.persistence.auth.RefreshTokenPersistence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenPersistence, UUID> {

    Optional<RefreshTokenPersistence> findByToken(String token);
}
