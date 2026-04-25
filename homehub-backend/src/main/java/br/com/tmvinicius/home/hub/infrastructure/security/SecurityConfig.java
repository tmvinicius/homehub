package br.com.tmvinicius.home.hub.infrastructure.security;

import br.com.tmvinicius.home.hub.domain.port.out.auth.PasswordEncoder;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import br.com.tmvinicius.home.hub.infrastructure.security.jwt.JwtProperties;
import br.com.tmvinicius.home.hub.infrastructure.security.jwt.JwtTokenAdapter;
import br.com.tmvinicius.home.hub.infrastructure.security.password.BCryptPasswordAdapter;
import br.com.tmvinicius.home.hub.infrastructure.security.password.BCryptProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, BCryptProperties.class})
public class SecurityConfig {

    @Bean
    public TokenProvider tokenProvider(JwtProperties properties){
        return new JwtTokenAdapter(properties);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(BCryptProperties bCryptProperties){
        return new BCryptPasswordEncoder(bCryptProperties.strength());
    }

    @Bean
    public PasswordEncoder passwordEncoder(BCryptPasswordEncoder encoder){
        return new BCryptPasswordAdapter(encoder);
    }



}
