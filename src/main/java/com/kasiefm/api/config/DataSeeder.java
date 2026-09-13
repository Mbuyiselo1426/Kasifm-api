package com.kasiefm.api.config;

import com.kasiefm.api.model.Show;
import com.kasiefm.api.repository.ShowRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Seeds the database with the real Kasie FM weekday (Mon-Fri) lineup.
 * Re-syncs on every startup (clears the table first) rather than a one-time
 * seed, so editing the list below is enough to update production on the
 * next deploy - no manual DB steps. Delete this class once you have a
 * proper admin endpoint or CSV import for managing the schedule.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final ShowRepository showRepository;

    public DataSeeder(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    @Override
    public void run(String... args) {
        if (showRepository.count() > 0) {
            return;
        }

        showRepository.save(new Show(
                "Whisper In The Dark",
                "TBC",
                LocalTime.of(0, 0),
                LocalTime.of(3, 0),
                "Music, topics and dedications through the night."
        ));

        showRepository.save(new Show(
                "Vuka Kasie-Rise and Shine",
                "TBC",
                LocalTime.of(3, 0),
                LocalTime.of(6, 0),
                "The early wake-up show, Monday to Friday."
        ));

        showRepository.save(new Show(
                "Asiye 6-9 Breakfast Show",
                "TBC",
                LocalTime.of(6, 0),
                LocalTime.of(9, 0),
                "Soweto's breakfast show - news, music and conversation."
        ));

        showRepository.save(new Show(
                "Morning Essentials",
                "TBC",
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                "Gender, women, health, home, children, religion and motivation."
        ));

        showRepository.save(new Show(
                "Semphete",
                "Mokaptene",
                LocalTime.of(12, 0),
                LocalTime.of(15, 0),
                "Community development, arts, disability, small business and NGOs."
        ));

        showRepository.save(new Show(
                "#HomeDrive",
                "Napo",
                LocalTime.of(15, 0),
                LocalTime.of(18, 0),
                "The drive-home soundtrack and youth-focused topics."
        ));

        showRepository.save(new Show(
                "Kasie Talk",
                "MaZulu",
                LocalTime.of(18, 0),
                LocalTime.of(21, 0),
                "News, current affairs and community issues."
        ));
    }
}
