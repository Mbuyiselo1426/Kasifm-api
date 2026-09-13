package com.kasiefm.api.config;

import com.kasiefm.api.model.Show;
import com.kasiefm.api.repository.ShowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DataJpaTest(properties = "app.seed.enabled=true", showSql = false)
@ActiveProfiles("test")
@Import(DataSeeder.class)
class DataSeederTest {
    @Autowired private DataSeeder seeder;
    @Autowired private ShowRepository repository;

    @BeforeEach
    void clearIsolatedTestDatabase() {
        // The enabled CommandLineRunner also runs during test context startup.
        // Reset only this embedded H2 fixture, inside the test rollback transaction.
        repository.deleteAllInBatch();
    }

    private static final List<String> WEEKDAY = List.of(
            "Whisper in the Dark", "Vuka Kasie", "Asiye 6-9 Breakfast Show",
            "Morning Essentials", "Semphete", "Home Drive with Napo", "Kasie Talk", "Late Night Affair");

    private List<String> expected(DayOfWeek day) {
        switch (day) {
            case FRIDAY:
                return List.of("Whisper in the Dark", "Vuka Kasie", "Asiye 6-9 Breakfast Show",
                        "Morning Essentials", "Semphete", "Home Drive with Napo", "The Weekend Takeover", "Club 971");
            case SATURDAY:
                return List.of("Midnight Express", "Kusempondo Zankomo", "Scoreline Show", "Ezakwantu",
                        "Urban Chart Show", "The Lifestyle Corner", "The Weekend Takeover", "Club 971");
            case SUNDAY:
                return List.of("Midnight Express", "Kusempondo Zankomo", "Asimdumise", "Centre Stage",
                        "Seven Colours", "Soul Food", "The Revival", "Late Night Affair");
            default:
                return WEEKDAY;
        }
    }

    @Test
    void seedsAll56VerifiedSlotsWithExactNamesTimesAndNoInventedMetadata() {
        seeder.run();
        assertEquals(56, repository.count());
        for (DayOfWeek day : DayOfWeek.values()) {
            List<Show> shows = repository.findByDayOfWeekOrderByStartTimeAscIdAsc(day);
            assertEquals(8, shows.size(), day.name());
            assertEquals(expected(day), shows.stream().map(Show::getName).toList(), day.name());
            for (int slot = 0; slot < 8; slot++) {
                Show show = shows.get(slot);
                assertEquals(day, show.getDayOfWeek());
                assertEquals(LocalTime.of(slot * 3, 0), show.getStartTime());
                assertEquals(LocalTime.of(((slot + 1) * 3) % 24, 0), show.getEndTime());
                assertNull(show.getPresenter());
                assertNull(show.getDescription());
            }
        }
    }

    @Test
    void preservesExistingIncompleteDatabaseWithoutBackfilling() {
        Show existing = repository.saveAndFlush(new Show("Existing custom show", "Existing presenter",
                LocalTime.of(9, 0), LocalTime.of(12, 0), "Existing description", null));
        seeder.run();
        assertEquals(1, repository.count());
        Show preserved = repository.findById(existing.getId()).orElseThrow();
        assertEquals("Existing custom show", preserved.getName());
        assertEquals("Existing presenter", preserved.getPresenter());
        assertEquals("Existing description", preserved.getDescription());
        assertNull(preserved.getDayOfWeek());
    }

    @Test
    void repeatedSeedingPreservesIdsAndUserEdits() {
        seeder.run();
        List<Long> ids = repository.findAll().stream().map(Show::getId).sorted().toList();
        Show edited = repository.findById(ids.get(0)).orElseThrow();
        edited.setName("User edited title");
        repository.saveAndFlush(edited);
        seeder.run();
        assertEquals(ids, repository.findAll().stream().map(Show::getId).sorted().toList());
        assertEquals("User edited title", repository.findById(edited.getId()).orElseThrow().getName());
    }

    @Test
    void seederBeanRequiresExplicitOptIn() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withBean(ShowRepository.class, () -> mock(ShowRepository.class))
                .withUserConfiguration(DataSeeder.class);
        runner.run(context -> assertEquals(0, context.getBeansOfType(DataSeeder.class).size()));
        runner.withPropertyValues("app.seed.enabled=false")
                .run(context -> assertEquals(0, context.getBeansOfType(DataSeeder.class).size()));
        runner.withPropertyValues("app.seed.enabled=true")
                .run(context -> assertEquals(1, context.getBeansOfType(DataSeeder.class).size()));
    }

    @Test
    void manualSqlContainsExactlyTheSame56VerifiedSlots() throws Exception {
        seeder.run();
        String sql = Files.readString(Path.of("docs/verified-weekly-schedule-data.sql"));
        Pattern row = Pattern.compile("\\('([A-Z]+)', '([^']+)', '(\\d{2}:\\d{2})', '(\\d{2}:\\d{2})', NULL, NULL\\)");
        Matcher matcher = row.matcher(sql);
        List<String> sqlRows = new ArrayList<>();
        while (matcher.find()) {
            sqlRows.add(matcher.group(1) + "|" + matcher.group(2) + "|" + matcher.group(3) + "|" + matcher.group(4));
        }
        List<String> seededRows = repository.findAll().stream().map(show -> show.getDayOfWeek() + "|"
                + show.getName() + "|" + show.getStartTime() + "|" + show.getEndTime()).sorted().toList();
        assertEquals(56, sqlRows.size());
        assertEquals(seededRows, sqlRows.stream().sorted().toList());
    }
}
