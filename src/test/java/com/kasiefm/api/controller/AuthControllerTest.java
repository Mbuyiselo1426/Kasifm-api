package com.kasiefm.api.controller;

import com.kasiefm.api.model.PresenterUser;
import com.kasiefm.api.model.UserRole;
import com.kasiefm.api.repository.PresenterUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private PresenterUserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach void setUp() {
        userRepository.deleteAll();
        userRepository.save(new PresenterUser("presenter", passwordEncoder.encode("correct-password"),
                "Lindiwe", UserRole.PRESENTER));
    }

    @Test void validLoginReturnsTokenAndNeverReturnsPasswordHash() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"presenter\",\"password\":\"correct-password\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.displayName", is("Lindiwe")))
                .andExpect(jsonPath("$.role", is("PRESENTER")))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        assertNotEquals("correct-password", userRepository.findByUsername("presenter").orElseThrow().getPasswordHash());
    }

    @Test void invalidPasswordAndUnknownUserReturnUnauthorized() throws Exception {
        for (String body : new String[]{
                "{\"username\":\"presenter\",\"password\":\"wrong\"}",
                "{\"username\":\"unknown\",\"password\":\"wrong\"}"}) {
            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }
    }
}
