package br.com.tmvinicius.home.hub.infrastructure.persistence;


import br.com.tmvinicius.home.hub.domain.port.out.auth.RefreshTokenRepository;
import br.com.tmvinicius.home.hub.domain.port.out.user.UserRepository;
import br.com.tmvinicius.home.hub.infrastructure.persistence.auth.adapter.RefreshTokenAdapter;
import br.com.tmvinicius.home.hub.infrastructure.persistence.auth.mapper.RefreshTokenPersistenceMapper;
import br.com.tmvinicius.home.hub.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import br.com.tmvinicius.home.hub.infrastructure.persistence.repository.UserJpaRepository;
import br.com.tmvinicius.home.hub.infrastructure.persistence.user.adapter.UserPersistenceAdapter;
import br.com.tmvinicius.home.hub.infrastructure.persistence.user.mapper.UserPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceConfig {

    @Bean
    public UserRepository userRepository(UserJpaRepository userJpaRepository,
                                         UserPersistenceMapper userPersistenceMapper){
        return new UserPersistenceAdapter(userJpaRepository,userPersistenceMapper);
    }

    @Bean
    public UserPersistenceMapper userPersistenceMapper(){
        return new UserPersistenceMapper();
    }

    @Bean
    public RefreshTokenRepository refreshTokenRepository(RefreshTokenJpaRepository refreshTokenJpaRepository,
                                                         RefreshTokenPersistenceMapper refreshTokenPersistenceMapper){
        return new RefreshTokenAdapter(refreshTokenJpaRepository, refreshTokenPersistenceMapper);
    }

    @Bean
    public RefreshTokenPersistenceMapper refreshTokenPersistenceMapper(){
        return new RefreshTokenPersistenceMapper();
    }


}
