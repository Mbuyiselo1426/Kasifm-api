package com.kasiefm.api.service;

import com.kasiefm.api.model.PresenterUser;
import com.kasiefm.api.model.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {
    @Test void expiredTokenIsRejected() {
        JwtService service = new JwtService("test-only-jwt-secret-must-be-at-least-32-bytes-long", -1);
        String token = service.createToken(new PresenterUser("presenter", "hash", "Presenter", UserRole.PRESENTER));
        assertThrows(ExpiredJwtException.class, () -> service.parse(token));
    }
}
