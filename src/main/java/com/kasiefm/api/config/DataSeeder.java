package com.kasiefm.api.config;

import com.kasiefm.api.model.Show;
import com.kasiefm.api.repository.ShowRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Weekly lineup manually verified by the project owner against
 * https://kasiefm971.co.za/shows.html, Monday through Sunday (2026-09-13).
 * Opt-in, empty databases only. Unknown presenters/descriptions remain null.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = false)
public class DataSeeder implements CommandLineRunner {
    private final ShowRepository showRepository;

    public DataSeeder(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (showRepository.count() > 0) {
            return;
        }
        List<Show> shows = new ArrayList<>(56);
        for (DayOfWeek day : EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.THURSDAY)) {
            addDay(shows, day,
                "Whisper in the Dark",
                "Vuka Kasie",
                "Asiye 6-9 Breakfast Show",
                "Morning Essentials",
                "Semphete",
                "Home Drive with Napo",
                "Kasie Talk",
                "Late Night Affair");
        }
        addDay(shows, DayOfWeek.FRIDAY,
                "Whisper in the Dark",
                "Vuka Kasie",
                "Asiye 6-9 Breakfast Show",
                "Morning Essentials",
                "Semphete",
                "Home Drive with Napo",
                "The Weekend Takeover",
                "Club 971");
        addDay(shows, DayOfWeek.SATURDAY,
                "Midnight Express",
                "Kusempondo Zankomo",
                "Scoreline Show",
                "Ezakwantu",
                "Urban Chart Show",
                "The Lifestyle Corner",
                "The Weekend Takeover",
                "Club 971");
        addDay(shows, DayOfWeek.SUNDAY,
                "Midnight Express",
                "Kusempondo Zankomo",
                "Asimdumise",
                "Centre Stage",
                "Seven Colours",
                "Soul Food",
                "The Revival",
                "Late Night Affair");
        showRepository.saveAll(shows);
    }

    private void addDay(List<Show> shows, DayOfWeek day, String... names) {
        for (int slot = 0; slot < names.length; slot++) {
            shows.add(new Show(names[slot], null, LocalTime.of(slot * 3, 0),
                    LocalTime.of(((slot + 1) * 3) % 24, 0), null, day));
        }
    }
}
