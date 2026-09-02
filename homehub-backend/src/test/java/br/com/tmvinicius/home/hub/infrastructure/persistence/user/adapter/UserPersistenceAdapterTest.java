package br.com.tmvinicius.home.hub.infrastructure.persistence.user.adapter;


import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.model.user.User;
import br.com.tmvinicius.home.hub.domain.model.user.UserRole;
import br.com.tmvinicius.home.hub.infrastructure.persistence.repository.UserJpaRepository;
import br.com.tmvinicius.home.hub.infrastructure.persistence.user.UserPersistence;
import br.com.tmvinicius.home.hub.infrastructure.persistence.user.mapper.UserPersistenceMapper;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UserPersistenceAdapterTest {

        @Mock
        private UserJpaRepository userJpaRepository;

        @Mock
        private UserPersistenceMapper userPersistenceMapper;

        @InjectMocks
        private UserPersistenceAdapter userPersistenceAdapter;

        private User user;
        private Email email;
        private Password password;
        private UUID id = UUID.randomUUID();
        private UserPersistence userPersistence;

        @BeforeEach
        public void setUp(){

            password = new Password("Ab123home@");
            email = new Email("teste@homehub.com");
            user = new User(
                    id,
                    email,
                    password,
                    UserRole.USER,
                    true);

            userPersistence = new UserPersistence(
                    id,
                    email.getValue(),
                    password.getValue(),
                    UserRole.USER,
                    true);
        }

        @Test
        void shouldFindByEmailAndReturnOptionalUser(){
            when(userJpaRepository.findByEmail(user.getEmail().getValue())).thenReturn(userPersistence);
            when(userPersistenceMapper.entityToDomain(userPersistence)).thenReturn(user);

            Optional<User> result = userPersistenceAdapter.findByEmail(user.getEmail());

            assertTrue(result.isPresent());
            assertEquals(user, result.get());
            verify(userJpaRepository).findByEmail(user.getEmail().getValue());
            verify(userPersistenceMapper).entityToDomain(userPersistence);
        }

        @Test
        void shouldFindByEmailAndReturnOptionalEmpty(){
            Email email = new Email("missing@homehub.com");
            when(userJpaRepository.findByEmail(email.getValue())).thenReturn(null);

            Optional<User> result = userPersistenceAdapter.findByEmail(email);

            assertTrue(result.isEmpty());
            verify(userJpaRepository).findByEmail(email.getValue());
            verifyNoInteractions(userPersistenceMapper);
        }

        @Test
        void shouldFindByIdAndReturnOptionalUser(){
            when(userJpaRepository.findById(user.getId())).thenReturn(Optional.of(userPersistence));
            when(userPersistenceMapper.entityToDomain(userPersistence)).thenReturn(user);

            Optional<User> result = userPersistenceAdapter.findById(user.getId());

            assertTrue(result.isPresent());
            assertEquals(user, result.get());
            verify(userJpaRepository).findById(user.getId());
            verify(userPersistenceMapper).entityToDomain(userPersistence);
        }

        @Test
        void shouldFindByIdAndReturnOptionalEmpty(){
            when(userJpaRepository.findById(user.getId())).thenReturn(Optional.empty());

            Optional<User> result = userPersistenceAdapter.findById(user.getId());

            assertTrue(result.isEmpty());
            verify(userJpaRepository).findById(user.getId());
            verifyNoInteractions(userPersistenceMapper);
        }




















}
