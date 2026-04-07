package br.com.tmvinicius.home.hub.infrastructure.security.jwt;

import br.com.tmvinicius.home.hub.domain.model.user.User;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class JwtTokenAdapter implements TokenProvider {

    @Value("${jwt.secret}")
    String SECRET_KEY;
    @Value("${jwt.expiration}")
    long EXPIRATION_TIME;

    @Override
    public String generate(User user) {

        final Date expirationDate = new Date((new Date())
                .getTime() + EXPIRATION_TIME);

        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

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
