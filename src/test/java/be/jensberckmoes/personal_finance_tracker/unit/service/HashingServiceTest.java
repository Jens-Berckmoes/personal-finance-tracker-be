package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.exception.InvalidPasswordException;
import be.jensberckmoes.personal_finance_tracker.service.HashingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HashingServiceTest {

    @Autowired
    private HashingService hashingService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    public void givenPassword_whenHashPassword_thenReturnHashedPassword() {
        final String password = "password123";
        final String hashedPassword = hashingService.hashPassword(password);

        assertNotNull(hashedPassword);
        assertFalse(hashedPassword.isEmpty());
        assertNotEquals(password, hashedPassword);
    }

    @Test
    public void givenPassword_whenHashPassword_thenReturnHashedPasswordWithExpectedFormat() {
        final String password = "password123";
        final String hashedPassword = hashingService.hashPassword(password);

        assertTrue(hashedPassword.startsWith("$2a$")); // Check for bcrypt prefix
    }

    @Test
    public void givenSamePassword_whenHashPasswordTwice_thenHashesAreDifferent() {
        final String password = "password123";
        final String hashedPassword1 = hashingService.hashPassword(password);
        final String hashedPassword2 = hashingService.hashPassword(password);

        assertNotEquals(hashedPassword1, hashedPassword2); // Check that the hashes are different
    }

    @Test
    public void givenPassword_whenHashPassword_thenCanVerifyHash() {
        final String password = "password123";
        final String hashedPassword = hashingService.hashPassword(password);

        assertTrue(passwordEncoder.matches(password, hashedPassword)); // Verify the hashed password
    }

    @Test
    public void givenShortPassword_whenHashPassword_thenReturnHashedPassword() {
        final String password = "a";
        final String hashedPassword = hashingService.hashPassword(password);

        assertNotNull(hashedPassword);
        assertFalse(hashedPassword.isEmpty());
        assertNotEquals(password, hashedPassword);
    }

    @Test
    public void givenLongPassword_whenHashPassword_thenReturnHashedPassword() {
        final String password = "a".repeat(100);
        final String hashedPassword = hashingService.hashPassword(password);

        assertNotNull(hashedPassword);
        assertFalse(hashedPassword.isEmpty());
        assertNotEquals(password, hashedPassword);
    }

    @Test
    public void givenNullPassword_whenHashPassword_thenThrowException() {
        // Act and Assert
        final Exception exception = assertThrows(InvalidPasswordException.class, () -> hashingService.hashPassword(null));

        assertEquals("Password cannot be null", exception.getMessage());
    }

    @Test
    public void givenEmptyPassword_whenHashPassword_thenThrowException() {
        // Act and Assert
        final Exception exception = assertThrows(InvalidPasswordException.class, () -> hashingService.hashPassword(""));

        assertEquals("Password was empty", exception.getMessage());
    }

}
