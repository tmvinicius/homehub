package br.com.tmvinicius.home.hub.domain.model.user;


import br.com.tmvinicius.home.hub.domain.exception.user.InvalidUserException;

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

    public User(UUID id, Email email, Password password, UserRole role, Boolean active){
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
    }
    public User(UUID id, Email email, Password password) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
        this.role = UserRole.ADMIN;
        this.active = Boolean.TRUE;
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
            throw new InvalidUserException("Usuario não tem permissao!");
        }
        this.active = active;
    }

    public void validateUser(){
        if(!active) {
            throw new InvalidUserException("Usuario nao está ativo!");
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
