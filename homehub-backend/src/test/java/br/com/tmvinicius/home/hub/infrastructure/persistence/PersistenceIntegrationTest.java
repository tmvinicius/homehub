package br.com.tmvinicius.home.hub.infrastructure.persistence;

import br.com.tmvinicius.home.hub.domain.model.user.UserRole;
import br.com.tmvinicius.home.hub.infrastructure.persistence.auth.RefreshTokenPersistence;
import br.com.tmvinicius.home.hub.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import br.com.tmvinicius.home.hub.infrastructure.persistence.repository.UserJpaRepository;
import br.com.tmvinicius.home.hub.infrastructure.persistence.user.UserPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersistenceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("homehub_test")
                    .withUsername("homehub")
                    .withPassword("homehub");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RefreshTokenJpaRepository refreshTokenJpaRepository;

    @BeforeEach
    void cleanDatabase() {
        refreshTokenJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Test
    void shouldPersistAndFindUserByEmail() {
        UUID userId = UUID.randomUUID();
        UserPersistence user = new UserPersistence(
                userId,
                "user@homehub.local",
                "hashed-password",
                UserRole.USER,
                true
        );

        userJpaRepository.saveAndFlush(user);
        UserPersistence persistedUser = userJpaRepository.findByEmail("user@homehub.local");

        assertNotNull(persistedUser);
        assertAll(
                () -> assertEquals(userId, persistedUser.getId()),
                () -> assertEquals("user@homehub.local", persistedUser.getEmail()),
                () -> assertEquals(UserRole.USER, persistedUser.getUserRole()),
                () -> assertTrue(persistedUser.getActive())
        );
    }

    @Test
    void shouldPersistAndFindRefreshTokenByValue() {
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RefreshTokenPersistence refreshToken = new RefreshTokenPersistence(
                tokenId,
                userId,
                "opaque-refresh-token",
                Instant.now().plusSeconds(3600),
                false
        );

        refreshTokenJpaRepository.saveAndFlush(refreshToken);
        Optional<RefreshTokenPersistence> result =
                refreshTokenJpaRepository.findByToken("opaque-refresh-token");

        assertTrue(result.isPresent());
        assertAll(
                () -> assertEquals(tokenId, result.get().getId()),
                () -> assertEquals(userId, result.get().getUserId()),
                () -> assertFalse(result.get().isRevoked())
        );
    }

    @Test
    void shouldRejectDuplicatedRefreshTokenValue() {
        RefreshTokenPersistence first = new RefreshTokenPersistence(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "same-refresh-token",
                Instant.now().plusSeconds(3600),
                false
        );
        RefreshTokenPersistence second = new RefreshTokenPersistence(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "same-refresh-token",
                Instant.now().plusSeconds(3600),
                false
        );

        refreshTokenJpaRepository.saveAndFlush(first);

        assertThrows(DataIntegrityViolationException.class,
                () -> refreshTokenJpaRepository.saveAndFlush(second));
    }
}
