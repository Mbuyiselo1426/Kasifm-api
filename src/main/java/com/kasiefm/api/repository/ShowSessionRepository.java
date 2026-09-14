package com.kasiefm.api.repository;

import com.kasiefm.api.model.ShowSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ShowSessionRepository extends JpaRepository<ShowSession, Long> {
    Optional<ShowSession> findByShowIdAndSessionDate(Long showId, LocalDate sessionDate);
}
