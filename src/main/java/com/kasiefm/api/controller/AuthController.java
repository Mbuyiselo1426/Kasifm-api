package com.kasiefm.api.controller;

import com.kasiefm.api.model.LoginRequest;
import com.kasiefm.api.model.LoginResponse;
import com.kasiefm.api.model.PresenterUser;
import com.kasiefm.api.repository.PresenterUserRepository;
import com.kasiefm.api.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final PresenterUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(PresenterUserRepository userRepository, PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        PresenterUser user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new LoginResponse(jwtService.createToken(user), user.getDisplayName(), user.getRole());
    }
}
