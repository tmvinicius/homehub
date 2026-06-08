    package br.com.tmvinicius.home.hub.infrastructure.persistence.user.adapter;

    import br.com.tmvinicius.home.hub.domain.model.user.Email;
    import br.com.tmvinicius.home.hub.domain.model.user.User;
    import br.com.tmvinicius.home.hub.domain.port.out.user.UserRepository;
    import br.com.tmvinicius.home.hub.infrastructure.persistence.repository.UserJpaRepository;
    import br.com.tmvinicius.home.hub.infrastructure.persistence.user.mapper.UserPersistenceMapper;

    import java.util.Optional;
    import java.util.UUID;

    public class UserPersistenceAdapter implements UserRepository {

        private final UserJpaRepository userJpaRepository;
        private final UserPersistenceMapper userPersistenceMapper;

        public UserPersistenceAdapter(UserJpaRepository userJpaRepository, UserPersistenceMapper userPersistenceMapper){
            this.userJpaRepository = userJpaRepository;
            this.userPersistenceMapper = userPersistenceMapper;
        }

        @Override
        public Optional<User> findByEmail(Email email) {

            return Optional.ofNullable(userJpaRepository.findByEmail(email.getValue()))
                    .map(userPersistenceMapper::entityToDomain);
        }

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.of(userJpaRepository.findById(id).get())
                    .map(userPersistenceMapper::entityToDomain);
        }
    }
