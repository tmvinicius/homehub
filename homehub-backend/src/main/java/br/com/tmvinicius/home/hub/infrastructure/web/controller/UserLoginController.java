package br.com.tmvinicius.home.hub.infrastructure.web.controller;


import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.port.in.auth.LoginUseCase;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.request.user.UserLoginRequest;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.response.user.UserLoginResponse;
import br.com.tmvinicius.home.hub.infrastructure.web.mapper.AuthMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserLoginController {

    private final LoginUseCase loginUseCase;
    private final AuthMapper authMapper;

    public UserLoginController(LoginUseCase loginUseCase, AuthMapper authMapper){
        this.loginUseCase = loginUseCase;
        this.authMapper = authMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> userLogin(@RequestBody UserLoginRequest request){

        Email email = authMapper.toEmail(request);
        Password password = authMapper.toPassword(request);

        String token = loginUseCase.userLogin(email, password);

        return ResponseEntity.ok(new UserLoginResponse(token));
    }

}
