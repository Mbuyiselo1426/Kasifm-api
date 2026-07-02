package com.kasiefm.api.config;

import com.kasiefm.api.model.Show;
import com.kasiefm.api.repository.ShowRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Seeds the database with the same schedule that used to be hardcoded in
 * MainActivity.java (#HomeDrive, #Throwback, #Highlights, #NewsHour).
 * Edit the details below (presenter names, descriptions) to match the real
 * Kasie FM lineup, then delete this class once you have a proper admin
 * endpoint or CSV import for managing the schedule.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final ShowRepository showRepository;

    public DataSeeder(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    @Override
    public void run(String... args) {
        if (showRepository.count() > 0) {
            return; // already seeded, don't duplicate on every restart
        }

        showRepository.save(new Show(
                "#HomeDrive",
                "TBC",
                LocalTime.of(15, 0),
                LocalTime.of(18, 0),
                "The drive home soundtrack - the best of Kasie FM to close out your day."
        ));

        showRepository.save(new Show(
                "#Throwback",
                "TBC",
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                "Best of Kasie FM - a nostalgic run through classic tracks and moments."
        ));

        showRepository.save(new Show(
                "#Highlights",
                "TBC",
                LocalTime.of(20, 0),
                LocalTime.of(22, 0),
                "Today's highlights - the top moments from across the station."
        ));

        showRepository.save(new Show(
                "#NewsHour",
                "TBC",
                LocalTime.of(22, 0),
                LocalTime.of(23, 59, 59),
                "Community updates and news from around the kasi."
        ));
    }
}
