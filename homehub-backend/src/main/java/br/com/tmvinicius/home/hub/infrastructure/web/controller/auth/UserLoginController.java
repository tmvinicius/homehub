package br.com.tmvinicius.home.hub.infrastructure.web.controller.auth;


import br.com.tmvinicius.home.hub.domain.exception.auth.TokenInvalidException;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.port.in.auth.LoginUseCase;
import br.com.tmvinicius.home.hub.domain.port.in.auth.VerifyTokenUseCase;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.request.user.UserLoginRequest;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.response.user.UserLoginResponse;
import br.com.tmvinicius.home.hub.infrastructure.web.mapper.AuthMapper;
import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.parser.Entity;

@RestController
@RequestMapping("/api/auth")
public class UserLoginController {

    private final LoginUseCase loginUseCase;
    private final AuthMapper authMapper;
    private final VerifyTokenUseCase verifyTokenUseCase;


    public UserLoginController(LoginUseCase loginUseCase, AuthMapper authMapper, VerifyTokenUseCase verifyTokenUseCase){
        this.loginUseCase = loginUseCase;
        this.authMapper = authMapper;
        this.verifyTokenUseCase = verifyTokenUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> userLogin(@RequestBody UserLoginRequest request){

        Email email = authMapper.toEmail(request);
        Password password = authMapper.toPassword(request);

        String token = loginUseCase.userLogin(email, password);

        return ResponseEntity.ok(new UserLoginResponse(token));
    }


    @GetMapping("/verify")
    public ResponseEntity<Void> userVerify(@RequestHeader(value = "Authorization", required = false) String authHeader ){

        if (authHeader == null || authHeader.isBlank()) {
            throw new TokenInvalidException("Authorization header inexistente");
        }
        if (!authHeader.startsWith("Bearer ")) {
            throw new TokenInvalidException("Authorization header invalido");
        }

        String token = authHeader.replace("Bearer ", "");

        verifyTokenUseCase.verify(token);

        return ResponseEntity.ok().build();
    }

}
