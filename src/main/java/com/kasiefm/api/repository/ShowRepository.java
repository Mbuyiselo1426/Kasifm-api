package com.kasiefm.api.repository;

import com.kasiefm.api.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.DayOfWeek;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByDayOfWeekOrderByStartTimeAscIdAsc(DayOfWeek dayOfWeek);
}
