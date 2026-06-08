package br.com.tmvinicius.home.hub.infrastructure.persistence.auth;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenPersistence {


    @Id
    @Column(unique = true, nullable = false)
    private UUID id;

    @OneToOne
    @Column(unique = true, nullable = false)
    @JoinColumn(name = "users", referencedColumnName = "id")
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Boolean revoked;


}
