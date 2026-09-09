package br.com.tmvinicius.home.hub.infrastructure.security.filter;

import br.com.tmvinicius.home.hub.domain.exception.auth.TokenInvalidException;
import br.com.tmvinicius.home.hub.domain.model.auth.AuthenticatedUser;
import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.UserRole;
import br.com.tmvinicius.home.hub.domain.port.out.auth.TokenProvider;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private FilterChain filterChain;

    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtFilter = new JwtFilter(tokenProvider);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterChainWithoutAuthenticationWhenBearerTokenIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenProvider);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldAuthenticateRequestWhenBearerTokenIsValid() throws Exception {
        String token = "valid-token";
        AuthenticatedUser user = new AuthenticatedUser(
                UUID.randomUUID(),
                new Email("admin@homehub.local"),
                UserRole.ADMIN);

        when(tokenProvider.parseAndValidate(token)).thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertSame(user, authentication.getPrincipal());
        assertTrue(authentication.isAuthenticated());
        assertEquals(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                authentication.getAuthorities().stream().toList());

        verify(tokenProvider).parseAndValidate(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenTokenIsInvalid() throws Exception {
        String token = "invalid-token";
        when(tokenProvider.parseAndValidate(token))
                .thenThrow(new TokenInvalidException("Token invalido"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenProvider).parseAndValidate(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotReplaceExistingAuthentication() throws Exception {
        Authentication existingAuthentication = new UsernamePasswordAuthenticationToken(
                "existing-user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        String token = "valid-token";
        AuthenticatedUser parsedUser = new AuthenticatedUser(
                UUID.randomUUID(),
                new Email("admin@homehub.local"),
                UserRole.ADMIN);

        when(tokenProvider.parseAndValidate(token)).thenReturn(parsedUser);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        assertSame(existingAuthentication, SecurityContextHolder.getContext().getAuthentication());
        verify(tokenProvider).parseAndValidate(token);
        verify(filterChain).doFilter(request, response);
    }
}
