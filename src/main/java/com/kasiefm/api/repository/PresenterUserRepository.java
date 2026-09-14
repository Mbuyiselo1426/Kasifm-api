package com.kasiefm.api.repository;

import com.kasiefm.api.model.PresenterUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PresenterUserRepository extends JpaRepository<PresenterUser, Long> {
    Optional<PresenterUser> findByUsername(String username);
}
