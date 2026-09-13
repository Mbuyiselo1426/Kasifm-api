package com.kasiefm.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kasiefm.api.controller.ScheduleController;
import com.kasiefm.api.model.Show;
import com.kasiefm.api.model.ShowDto;
import com.kasiefm.api.repository.ShowRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ScheduleMapperTest {
    private Show show(long id, String start, String end) {
        Show show = new Show("Show " + id, "Presenter", LocalTime.parse(start), LocalTime.parse(end), "Description");
        show.setId(id);
        return show;
    }

    private ScheduleMapper at(String utc) {
        return new ScheduleMapper(Clock.fixed(Instant.parse("2026-09-13T" + utc + "Z"), ZoneOffset.UTC));
    }

    private Long selected(ScheduleMapper mapper, List<Show> shows) {
        List<ShowDto> result = mapper.map(shows);
        assertTrue(result.stream().filter(ShowDto::isCurrent).count() <= 1);
        return result.stream().filter(ShowDto::isCurrent).map(ShowDto::getId).findFirst().orElse(null);
    }

    @Test
    void stationTimezoneAndInclusiveStartExclusiveEnd() {
        List<Show> shows = Arrays.asList(show(1, "06:00", "09:00"), show(2, "09:00", "12:00"));
        assertNull(selected(at("03:59:59"), shows));
        assertEquals(1L, selected(at("04:00:00"), shows));
        assertEquals(1L, selected(at("06:59:59"), shows));
        assertEquals(2L, selected(at("07:00:00"), shows));
        assertNull(selected(at("10:00:00"), shows));
    }

    @Test
    void overnightAndZeroDuration() {
        List<Show> shows = Arrays.asList(show(1, "22:00", "02:00"), show(2, "00:00", "00:00"));
        assertEquals(1L, selected(at("20:00:00"), shows));
        assertEquals(1L, selected(at("22:00:00"), shows));
        assertEquals(1L, selected(at("23:59:59"), shows));
        assertNull(selected(at("00:00:00"), shows));
        assertNull(selected(at("19:59:59"), shows));
    }

    @Test
    void emptyScheduleAndGap() {
        assertTrue(at("07:00:00").map(Collections.emptyList()).isEmpty());
        assertNull(selected(at("07:00:00"), Arrays.asList(show(1, "06:00", "08:00"), show(2, "10:00", "12:00"))));
    }

    @Test
    void overlapWinnerAndSortingAreIndependentOfInputOrder() {
        Show a = show(3, "08:00", "12:00");
        Show b = show(1, "09:00", "10:00");
        Show c = show(2, "09:00", "11:00");
        for (List<Show> shows : Arrays.asList(Arrays.asList(a,b,c), Arrays.asList(a,c,b),
                Arrays.asList(b,a,c), Arrays.asList(b,c,a), Arrays.asList(c,a,b), Arrays.asList(c,b,a))) {
            assertEquals(1L, selected(at("07:30:00"), shows));
            assertEquals(Arrays.asList(3L,1L,2L), at("07:30:00").map(shows).stream().map(ShowDto::getId).toList());
        }
    }

    @Test
    void readsClockOnlyOncePerResponse() {
        Clock clock = mock(Clock.class);
        when(clock.instant()).thenReturn(Instant.parse("2026-09-13T07:00:00Z"));
        new ScheduleMapper(clock).map(Arrays.asList(show(1,"06:00","09:00"), show(2,"09:00","12:00")));
        verify(clock, times(1)).instant();
    }

    @Test
    void endpointsSerializeExactMarkerAndUseFullScheduleForDetails() throws Exception {
        ShowRepository repository = mock(ShowRepository.class);
        Show earlier = show(1,"06:00","09:00");
        Show current = show(2,"09:00","12:00");
        when(repository.findAllByOrderByStartTimeAsc()).thenReturn(Arrays.asList(current, earlier));
        when(repository.findById(2L)).thenReturn(Optional.of(current));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ScheduleController(repository, at("07:00:00"))).build();
        String json = mvc.perform(get("/api/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isCurrent").value(false))
                .andExpect(jsonPath("$[1].isCurrent").value(true))
                .andExpect(jsonPath("$[1].startTime").value("09:00"))
                .andReturn().getResponse().getContentAsString();
        JsonNode item = new ObjectMapper().readTree(json).get(1);
        assertTrue(item.get("isCurrent").isBoolean());
        assertFalse(item.has("current"));
        mvc.perform(get("/api/schedule/2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.isCurrent").value(true));
    }
}
