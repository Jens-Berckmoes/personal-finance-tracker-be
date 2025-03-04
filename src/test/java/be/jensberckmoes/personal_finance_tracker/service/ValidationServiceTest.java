package be.jensberckmoes.personal_finance_tracker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ValidationServiceTest {

    @Autowired
    private ValidationService validationService;

    @Test
    public void givenValidEmail_whenIsValidEmail_thenReturnTrue() {
        assertTrue(validationService.isValidEmail("test@example.com"));
    }

    @Test
    public void givenInvalidEmail_whenIsValidEmail_thenReturnFalse() {
        assertFalse(validationService.isValidEmail("invalid-email"));
    }

    @Test
    public void givenValidUsername_whenIsValidUsername_thenReturnTrue() {
        assertTrue(validationService.isValidUsername("testuser"));
    }

    @Test
    public void givenEmptyUsername_whenIsValidUsername_thenReturnFalse() {
        assertFalse(validationService.isValidUsername(""));
    }

    @Test
    public void givenNullUsername_whenIsValidUsername_thenReturnFalse() {
        assertFalse(validationService.isValidUsername(null));
    }

}

