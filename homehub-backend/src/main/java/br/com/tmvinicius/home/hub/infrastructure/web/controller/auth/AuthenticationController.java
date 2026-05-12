package br.com.tmvinicius.home.hub.infrastructure.web.controller.auth;


import br.com.tmvinicius.home.hub.domain.model.auth.AuthenticatedUser;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.port.in.auth.LoginUseCase;
import br.com.tmvinicius.home.hub.domain.port.in.auth.VerifyTokenUseCase;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.request.user.UserLoginRequest;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.response.user.MeResponse;
import br.com.tmvinicius.home.hub.infrastructure.web.dto.response.user.UserLoginResponse;
import br.com.tmvinicius.home.hub.infrastructure.web.mapper.AuthMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import static br.com.tmvinicius.home.hub.infrastructure.security.jwt.BearerTokenExtractor.extractBearerToken;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final LoginUseCase loginUseCase;
    private final AuthMapper authMapper;
    private final VerifyTokenUseCase verifyTokenUseCase;


    public AuthenticationController(LoginUseCase loginUseCase,
                                    AuthMapper authMapper,
                                    VerifyTokenUseCase verifyTokenUseCase
                                    ){
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

        String token = extractBearerToken(authHeader);

        verifyTokenUseCase.verify(token);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> userMe(@AuthenticationPrincipal AuthenticatedUser user){

        MeResponse response = new MeResponse(
                user.getUserId(),
                user.getEmail().getValue(),
                user.getRole().name()
        );

        return ResponseEntity.ok(response);

    }

}
