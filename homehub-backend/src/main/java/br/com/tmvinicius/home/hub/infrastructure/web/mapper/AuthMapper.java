package br.com.tmvinicius.home.hub.infrastructure.web.mapper;

import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.request.user.UserLoginRequest;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.response.user.UserLoginResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public Email toEmail(UserLoginRequest request){
        return new Email(request.email());
    }

    public Password toPassword(UserLoginRequest request){
        return Password.of(request.password());
    }

    public UserLoginResponse userLoginResponse(String token){
        return new UserLoginResponse(token);
    }

}
