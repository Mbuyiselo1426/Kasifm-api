package com.kasiefm.api.repository;

import com.kasiefm.api.model.ShowSession;
import com.kasiefm.api.model.ShowSessionHistoryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShowSessionRepository extends JpaRepository<ShowSession, Long> {
    Optional<ShowSession> findByShowIdAndSessionDate(Long showId, LocalDate sessionDate);

    @Query("""
            select new com.kasiefm.api.model.ShowSessionHistoryDto(
                s.id, s.show.id, s.show.name, s.sessionDate, s.startsAt, s.endsAt,
                (select count(m.id) from Message m where m.showSession = s))
            from ShowSession s
            where (:currentSessionId is null or s.id <> :currentSessionId)
            order by s.startsAt desc, s.id desc
            """)
    List<ShowSessionHistoryDto> findRecentHistory(@Param("currentSessionId") Long currentSessionId,
                                                   Pageable pageable);
}
