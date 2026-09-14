package com.kasiefm.api.controller;

import com.kasiefm.api.model.*;
import com.kasiefm.api.repository.MessageRepository;
import com.kasiefm.api.repository.PresenterUserRepository;
import com.kasiefm.api.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MessageControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private MessageRepository messageRepository;
    @Autowired private PresenterUserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    @BeforeEach void setUp() { messageRepository.deleteAll(); userRepository.deleteAll(); }

    @Test void createPersistsNewMessageWithServerFieldsAndSongDetails() throws Exception {
        Instant before = Instant.now();
        mockMvc.perform(post("/api/messages").contentType(MediaType.APPLICATION_JSON).content("""
                {"senderName":"Mbuyiselo","category":"SONG_REQUEST","message":"For my family",
                 "songTitle":"Kasi Anthem","artist":"Napo"}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senderName", is("Mbuyiselo")))
                .andExpect(jsonPath("$.category", is("SONG_REQUEST")))
                .andExpect(jsonPath("$.songTitle", is("Kasi Anthem")))
                .andExpect(jsonPath("$.status", is("NEW")))
                .andExpect(jsonPath("$.createdAt").exists());
        Message stored = messageRepository.findAll().get(0);
        assertEquals(MessageStatus.NEW, stored.getStatus());
        assertEquals("Kasi Anthem", stored.getSongTitle());
        assertFalse(stored.getCreatedAt().isBefore(before));
    }

    @Test void createRejectsInvalidRequiredFieldsAndCategory() throws Exception {
        for (String body : new String[]{
                "{\"senderName\":\" \",\"category\":\"OTHER\",\"message\":\"Hi\"}",
                "{\"senderName\":\"Name\",\"category\":\"OTHER\",\"message\":\" \"}",
                "{\"senderName\":\"Name\",\"category\":\"SONG_REQUEST\",\"message\":\"Hi\"}",
                "{\"senderName\":\"Name\",\"category\":\"NOT_A_CATEGORY\",\"message\":\"Hi\"}"}) {
            mockMvc.perform(post("/api/messages").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        assertEquals(0, messageRepository.count());
    }

    @Test void getMessagesReturnsNewestFirst() throws Exception {
        Message older = messageRepository.save(new Message("Older", MessageCategory.OTHER, "First", null, null));
        Thread.sleep(2L);
        Message newer = messageRepository.save(new Message("Newer", MessageCategory.PRAYER_REQUEST, "Second", null, null));
        mockMvc.perform(get("/api/messages").header("Authorization", "Bearer " + presenterToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(newer.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(older.getId().intValue())));
    }

    @Test void presenterEndpointsRequireValidTokenButListenerSubmissionRemainsPublic() throws Exception {
        mockMvc.perform(post("/api/messages").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senderName\":\"Listener\",\"category\":\"OTHER\",\"message\":\"Hello\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/messages")).andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/messages/{id}/status", messageRepository.findAll().get(0).getId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"READ\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/messages/{id}/status", messageRepository.findAll().get(0).getId())
                .header("Authorization", "Bearer " + presenterToken()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"READ\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status", is("READ")));
    }

    private String presenterToken() {
        PresenterUser user = userRepository.save(new PresenterUser("presenter", passwordEncoder.encode("correct-password"),
                "Presenter", UserRole.PRESENTER));
        return jwtService.createToken(user);
    }
}
