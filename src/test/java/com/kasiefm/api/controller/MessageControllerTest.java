package com.kasiefm.api.controller;

import com.kasiefm.api.model.*;
import com.kasiefm.api.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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

    @BeforeEach void setUp() { messageRepository.deleteAll(); }

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
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(newer.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(older.getId().intValue())));
    }
}
