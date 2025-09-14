package uk.casey.utils;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class JwtUtilTest {

    @Test
    void generateTokenAndValidateSuccess() {
        UUID userId = UUID.randomUUID();
        String username = "unitUser";
        String token = JwtUtil.generateToken(userId, username);

        assertNotNull(token);
        assertTrue(JwtUtil.validateToken(token));
    }

    @Test
    void validateInvalidTokenReturnsFalse() {
        String invalidToken = "lookImAnInvalidToken";
        assertFalse(JwtUtil.validateToken(invalidToken));
    }
}
