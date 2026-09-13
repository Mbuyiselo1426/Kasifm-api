package com.kasiefm.api.controller;

import com.kasiefm.api.model.Show;
import com.kasiefm.api.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShowRepository showRepository;

    private Long showId;

    @BeforeEach
    void setUp() {
        showRepository.deleteAll();
        Show show = showRepository.save(new Show(
                "Original Show",
                "Original Presenter",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "Original description"
        ));
        showId = show.getId();
    }

    @Test
    void updateShowPersistsChanges() throws Exception {
        mockMvc.perform(put("/api/schedule/{id}", showId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Show",
                                  "presenter": "Updated Presenter",
                                  "startTime": "10:00",
                                  "endTime": "12:00",
                                  "description": "Updated description"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(showId.intValue())))
                .andExpect(jsonPath("$.name", is("Updated Show")))
                .andExpect(jsonPath("$.presenter", is("Updated Presenter")))
                .andExpect(jsonPath("$.startTime", is("10:00")))
                .andExpect(jsonPath("$.endTime", is("12:00")))
                .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    void updateShowRejectsInvalidTimeRange() throws Exception {
        mockMvc.perform(put("/api/schedule/{id}", showId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Show",
                                  "presenter": "Updated Presenter",
                                  "startTime": "12:00",
                                  "endTime": "10:00",
                                  "description": "Updated description"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
