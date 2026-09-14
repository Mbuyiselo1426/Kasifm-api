package com.kasiefm.api.config;

import com.kasiefm.api.model.PresenterUser;
import com.kasiefm.api.model.UserRole;
import com.kasiefm.api.repository.PresenterUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PresenterBootstrap {
    @Bean CommandLineRunner bootstrapPresenter(PresenterUserRepository repository, PasswordEncoder encoder,
            @Value("${app.bootstrap.username}") String username,
            @Value("${app.bootstrap.password}") String password,
            @Value("${app.bootstrap.display-name}") String displayName,
            @Value("${app.bootstrap.role}") UserRole role) {
        return args -> {
            if (!username.isBlank() && !password.isBlank() && repository.findByUsername(username).isEmpty()) {
                repository.save(new PresenterUser(username, encoder.encode(password), displayName, role));
            }
        };
    }
}
