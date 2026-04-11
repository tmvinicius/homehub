package br.com.tmvinicius.home.hub.infrastructure.security.jwt;

import br.com.tmvinicius.home.hub.domain.model.user.User;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtTokenAdapter implements TokenProvider {

    private final String secret;
    private final long expiration;

    public JwtTokenAdapter(JwtProperties properties){
        this.secret = properties.secret();
        this.expiration = properties.expiration();
    }


    @Override
    public String generate(User user) {

        final Date expirationDate = new Date((new Date())
                .getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .setSubject(user.getEmail().toString())
                .claim("id", user.getId().toString())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return token;
    }
}
