package br.com.tmvinicius.home.hub.infrastructure.security.jwt;

import br.com.tmvinicius.home.hub.domain.exception.auth.TokenInvalidException;
import br.com.tmvinicius.home.hub.domain.model.auth.AuthenticatedUser;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.User;
import br.com.tmvinicius.home.hub.domain.model.user.UserRole;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

public class JwtTokenAdapter implements TokenProvider {

    private final String secret;
    private final long expiration;

    public JwtTokenAdapter(JwtProperties properties){
        this.secret = properties.secret();
        this.expiration = properties.expiration();
    }

    public SecretKey getKey(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    @Override
    public String generate(User user) {

        final Date expirationDate = new Date((new Date())
                .getTime() + expiration);

        String token = Jwts.builder()
                .setSubject(user.getEmail().toString())
                .claim("id", user.getId().toString())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();

        return token;
    }

    public Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    @Override
    public AuthenticatedUser parseAndValidate(String token) {

        try {
            Claims claims = extractAllClaims(token);
            UUID userId = UUID.fromString((String) claims.get("id"));
            Email email = new Email(claims.getSubject());
            UserRole role = UserRole.valueOf((String) claims.get("role"));

            return new AuthenticatedUser(userId, email, role);

        } catch (Exception e) {
            throw new TokenInvalidException("Token invalido");
        }

    }
}
