package br.com.tmvinicius.home.hub.domain.model.user;


import br.com.tmvinicius.home.hub.domain.exception.InvalidPasswordException;

import java.util.Objects;

public class Password {

    private String password;


    public Password(){}

    public Password(String password) {
        validatePassword(password);
        this.password = password;
    }

    public static Password of(String rawPassword){
        validatePassword(rawPassword);
        return new Password(rawPassword);
    }

    public static Password fromHash(String hash) {
        Password password = new Password();
        password.password = hash;
        return password;
    }



    public String getValue(){
        return this.password;
    }

    public void setPassword(String password){
        validatePassword(password);
        this.password = password;
    }

    public static void validatePassword(String password){

        final String PASSWORD_PATTERN =
                "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\\\d)(?=.*[@#$%^&+=!]).{6,15}$";

        if(!password.matches(PASSWORD_PATTERN)){
            throw new InvalidPasswordException("Senha inválida!");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Password password1 = (Password) o;
        return Objects.equals(password, password1.password);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(password);
    }

    @Override
    public String toString() {
        return "Password{***}";
    }
}
