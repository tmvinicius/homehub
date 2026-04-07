package br.com.tmvinicius.home.hub.domain.model.user;


import br.com.tmvinicius.home.hub.domain.exception.InvalidUserException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class User {

    private UUID id;
    private Email email;
    private Password password;
    private LocalDateTime createdAt;
    private UserRole role;
    private Boolean active;

    public User(UUID id, Email email, Password password){
        this.id = id;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId(){
        return this.id;
    }

    public Email getEmail(){
        return email;
    }

    public void setEmail(Email email){
        this.email = email;
    }

    public Password getPassword(){
        return this.password;
    }

    public void setPassword(Password password){
        this.password = password;
    }

    public Boolean getActive() {
        return active;
    }

    public UserRole getRole() {
        return role;
    }

    public void setActive(Boolean active) {
        if(role != UserRole.ADMIN){
            throw new InvalidUserException("Usuario não tem permissão!");
        }
        this.active = active;
    }

    public void validateUser(){
        if(!active) {
            throw new InvalidUserException("Usuario não está ativo!");
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email) && Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }

    @Override
    public String toString() {
        return "User{" +
                "email=" + email +
                ", password=" + password +
                '}';
    }
}
