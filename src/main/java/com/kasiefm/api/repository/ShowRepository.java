package com.kasiefm.api.repository;

import com.kasiefm.api.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    // Handy for Phase 2 - lets the Android app ask "what's on right now?"
    List<Show> findAllByOrderByStartTimeAsc();
}
