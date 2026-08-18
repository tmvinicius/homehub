package br.com.tmvinicius.home.hub.infrastructure.persistence.auth.adapter;

import br.com.tmvinicius.home.hub.domain.model.auth.RefreshToken;
import br.com.tmvinicius.home.hub.infrastructure.persistence.auth.RefreshTokenPersistence;
import br.com.tmvinicius.home.hub.infrastructure.persistence.auth.mapper.RefreshTokenPersistenceMapper;
import br.com.tmvinicius.home.hub.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenAdapterTest {

    @Mock
    private RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Mock
    private RefreshTokenPersistenceMapper refreshTokenPersistenceMapper;

    @InjectMocks
    private RefreshTokenAdapter refreshTokenAdapter;

    private RefreshToken refreshToken;
    private RefreshTokenPersistence refreshTokenPersistence;

    @BeforeEach
    void setUp(){
        refreshToken = new RefreshToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "token",
                Instant.now().plusSeconds(300),
                Boolean.FALSE);

        refreshTokenPersistence = new RefreshTokenPersistence(
                refreshToken.getId(),
                refreshToken.getUserId(),
                refreshToken.getToken(),
                refreshToken.getExpiresAt(),
                Boolean.FALSE);
    }

    @Test
    void shouldSaveRefreshTokenAndReturnRefreshToken(){
        when(refreshTokenPersistenceMapper.domainToEntity(refreshToken)).thenReturn(refreshTokenPersistence);
        when(refreshTokenPersistenceMapper.entityToDomain(refreshTokenPersistence)).thenReturn(refreshToken);

        RefreshToken result = refreshTokenAdapter.save(refreshToken);

        assertEquals(refreshToken, result);
        verify(refreshTokenPersistenceMapper).domainToEntity(refreshToken);
        verify(refreshTokenJpaRepository).save(refreshTokenPersistence);
        verify(refreshTokenPersistenceMapper).entityToDomain(refreshTokenPersistence);
    }

    @Test
    void shouldFindByTokenAndReturnRefreshToken(){
        when(refreshTokenJpaRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshTokenPersistence));
        when(refreshTokenPersistenceMapper.entityToDomain(refreshTokenPersistence)).thenReturn(refreshToken);

        Optional<RefreshToken> result = refreshTokenAdapter.findByToken(refreshToken.getToken());


        assertTrue(result.isPresent());
        assertEquals(refreshToken,result.get());
        verify(refreshTokenJpaRepository).findByToken(refreshToken.getToken());
        verify(refreshTokenPersistenceMapper).entityToDomain(refreshTokenPersistence);
    }

    @Test
    void shouldReturnEmptyWhenRefreshTokenNotExist(){
        when(refreshTokenJpaRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.empty());

        Optional<RefreshToken> result = refreshTokenAdapter.findByToken(refreshToken.getToken());

        assertTrue(result.isEmpty());
        verify(refreshTokenJpaRepository).findByToken(refreshToken.getToken());
        verifyNoInteractions(refreshTokenPersistenceMapper);
    }









}
