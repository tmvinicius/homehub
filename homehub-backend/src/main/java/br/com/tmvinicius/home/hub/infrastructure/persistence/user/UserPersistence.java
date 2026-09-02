package br.com.tmvinicius.home.hub.infrastructure.persistence.user;


import br.com.tmvinicius.home.hub.domain.model.user.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPersistence {

    @Id
    @Column(unique = true)
    private UUID id;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private UserRole userRole;

    @Column(nullable = false)
    private Boolean active;


}
