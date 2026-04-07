package br.com.tmvinicius.home.hub.domain.model.user;

import br.com.tmvinicius.home.hub.domain.exception.InvalidEmailException;

import java.util.Objects;


public class Email {

    private String email;

    public Email(String email){

        validateEmail(email);

        this.email = email;
    }

    public String getEmail(){
        return this.email;
    }

    public void setEmail(String email){
        validateEmail(email);
        this.email = email;
    }

    public void validateEmail(String email){

        final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        if (!email.matches(EMAIL_PATTERN)){
            throw new InvalidEmailException("O email inserido é inválido!");
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Email email1 = (Email) o;
        return Objects.equals(email, email1.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }

    @Override
    public String toString() {
        return "Email{" +
                "email='" + email + '\'' +
                '}';
    }
}
