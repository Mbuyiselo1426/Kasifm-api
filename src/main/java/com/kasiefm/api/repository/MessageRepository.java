package com.kasiefm.api.repository;

import com.kasiefm.api.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByOrderByCreatedAtDescIdDesc();
    List<Message> findByShowSessionIdOrderByCreatedAtDescIdDesc(Long showSessionId);
}
