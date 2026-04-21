package br.com.tmvinicius.home.hub.infrastructure.security.filter;

import br.com.tmvinicius.home.hub.domain.model.auth.AuthenticatedUser;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    public JwtFilter(TokenProvider tokenProvider){
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = processToken(request);

        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private Authentication processToken(HttpServletRequest request){

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return null;
        }

        final String jwtToken = authHeader.substring(7);

        AuthenticatedUser user;
        try {
            user = tokenProvider.parseAndValidate(jwtToken);
        } catch (Exception e) {
            return null;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return null;
        }

        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UsernamePasswordAuthenticationToken(user, null, authorities
        );
    }






}
