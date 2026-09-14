package com.kasiefm.api.config;

import com.kasiefm.api.model.PresenterUser;
import com.kasiefm.api.repository.PresenterUserRepository;
import com.kasiefm.api.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final PresenterUserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, PresenterUserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                              FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String username = jwtService.parse(authorization.substring(7)).getSubject();
            PresenterUser user = userRepository.findByUsername(username).orElse(null);
            if (user != null && user.isEnabled()) {
                var authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // Leave the context anonymous; protected routes return the configured 401 response.
        }
        filterChain.doFilter(request, response);
    }
}
