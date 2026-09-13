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
import java.time.DayOfWeek;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

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
                "Original description", DayOfWeek.MONDAY
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
                                  "dayOfWeek": "FRIDAY",
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
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.dayOfWeek", is("FRIDAY")));
        assertEquals(DayOfWeek.FRIDAY, showRepository.findById(showId).orElseThrow().getDayOfWeek());
    }

    @Test
    void updateShowRejectsInvalidTimeRange() throws Exception {
        mockMvc.perform(put("/api/schedule/{id}", showId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Show",
                                  "dayOfWeek": "FRIDAY",
                                  "presenter": "Updated Presenter",
                                  "startTime": "12:00",
                                  "endTime": "12:00",
                                  "description": "Updated description"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAcceptsOvernightAndPersistsDay() throws Exception {
        mockMvc.perform(put("/api/schedule/{id}", showId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Night show","dayOfWeek":"FRIDAY","startTime":"22:00","endTime":"02:00"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayOfWeek").value("FRIDAY"))
                .andExpect(jsonPath("$.endTime").value("02:00"));
        Show stored = showRepository.findById(showId).orElseThrow();
        assertEquals(DayOfWeek.FRIDAY, stored.getDayOfWeek());
        assertEquals(LocalTime.of(2, 0), stored.getEndTime());
    }

    @Test
    void invalidMissingNullAndNumericDaysReturnBadRequest() throws Exception {
        for (String field : List.of("", "\"dayOfWeek\":null,", "\"dayOfWeek\":\"FUNDAY\",",
                "\"dayOfWeek\":1,", "\"dayOfWeek\":\"monday\",")) {
            mockMvc.perform(put("/api/schedule/{id}", showId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{" + field + "\"name\":\"Show\",\"startTime\":\"09:00\",\"endTime\":\"12:00\"}"))
                    .andExpect(status().isBadRequest());
        }
        assertEquals(DayOfWeek.MONDAY, showRepository.findById(showId).orElseThrow().getDayOfWeek());
    }

    @Test
    void repositoryFiltersDayAndOrdersByStartThenId() {
        Show early = showRepository.save(new Show("Early", null, LocalTime.of(6,0), LocalTime.of(8,0), null, DayOfWeek.MONDAY));
        Show sunday = showRepository.save(new Show("Sunday", null, LocalTime.of(6,0), LocalTime.of(8,0), null, DayOfWeek.SUNDAY));
        showRepository.save(new Show("Undated legacy row", null, LocalTime.of(0,0), LocalTime.of(1,0), null, null));
        assertEquals(List.of(early.getId(), showId), showRepository.findByDayOfWeekOrderByStartTimeAscIdAsc(DayOfWeek.MONDAY)
                .stream().map(Show::getId).toList());
        assertEquals(List.of(sunday.getId()), showRepository.findByDayOfWeekOrderByStartTimeAscIdAsc(DayOfWeek.SUNDAY)
                .stream().map(Show::getId).toList());
    }
}
