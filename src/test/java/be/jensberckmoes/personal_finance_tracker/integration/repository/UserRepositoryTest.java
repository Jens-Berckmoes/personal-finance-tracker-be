package be.jensberckmoes.personal_finance_tracker.integration.repository;

import be.jensberckmoes.personal_finance_tracker.model.User;
import be.jensberckmoes.personal_finance_tracker.repository.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .password("Password123!")
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    @Test
    public void givenUser_whenSave_thenPersistUser() {
        // Act
        final User savedUser = userRepository.save(user);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("Password123!", savedUser.getPassword());
        assertEquals("test@example.com", savedUser.getEmail());
    }

    @Test
    public void givenNullUsername_whenSave_thenThrowException() {
        // Arrange
        user.setUsername(null);

        // Act & Assert
        assertThrows(Exception.class, () -> userRepository.save(user));
    }

    @Test
    public void givenTooShortUsername_whenSave_thenThrowException() {
        // Arrange
        user.setUsername("Ab");

        final ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> userRepository.save(user));

        // Check the exception message
        assertTrue(exception.getMessage().contains("Username must be between 3 and 20 characters."));
    }

    @Test
    public void givenExactlyMinimumLengthUsername_whenSave_thenPasses() {
        // Arrange
        user.setUsername("Abc");

        final User savedUser = userRepository.save(user);

        // Check the exception message
        assertEquals("Abc", savedUser.getUsername());
    }

    @Test
    public void givenOneMoreThanMinimumLengthUsername_whenSave_thenPasses() {
        // Arrange
        user.setUsername("a".repeat(4));

        // Act & Assert
        final User savedUser = userRepository.save(user);

        assertEquals("aaaa", savedUser.getUsername());
    }

    @Test
    public void givenOneLessThanMaximumLengthUsername_whenSave_thenPasses() {
        // Arrange
        user.setUsername("A".repeat(19));

        final User savedUser = userRepository.save(user);

        // Check the exception message
        assertEquals("A".repeat(19), savedUser.getUsername());
    }

    @Test
    public void givenExactlyMaximumLengthUsername_whenSave_thenPasses() {
        // Arrange
        user.setUsername("A".repeat(20));

        final User savedUser = userRepository.save(user);

        // Check the exception message
        assertEquals("A".repeat(20), savedUser.getUsername());
    }

    @Test
    public void givenOneMoreThanMaximumLengthUsername_whenSave_thenThrowException() {
        // Arrange
        user.setUsername("a".repeat(21));

        // Act & Assert
        final ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> userRepository.save(user));

        // Check the exception message
        assertTrue(exception.getMessage().contains("Username must be between 3 and 20 characters."));
    }

    @Test
    public void givenInvalidCharacterInUsername_whenSave_thenThrowException() {
        // Arrange
        user.setUsername("Ab!".repeat(4));

        // Act & Assert
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> userRepository.save(user));

        // Check the exception message
        assertTrue(exception.getMessage().contains("Username can only contain letters, numbers, dots, or underscores."));
    }

    @Test
    public void givenNullPassword_whenSave_thenThrowException() {
        // Arrange
        user.setPassword(null);

        // Act & Assert
        assertThrows(Exception.class, () -> userRepository.save(user));
    }

    @Test
    public void givenTooShortPassword_whenSave_thenThrowException() {
        // Arrange
        user.setPassword("Ab1!" + "A".repeat(7));//11

        final ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> userRepository.save(user));

        // Check the exception message
        assertTrue(exception.getMessage().contains("Password must be between 12 and 64 characters."));
    }

    @Test
    public void givenTooLongPassword_whenSave_thenThrowException() {
        // Arrange
        user.setPassword("Ab1!" + "A".repeat(61));//65

        // Act & Assert
        final ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> userRepository.save(user));

        // Check the exception message
        assertTrue(exception.getMessage().contains("Password must be between 12 and 64 characters."));
    }

    @Test
    public void givenInvalidCharacterInPassword_whenSave_thenThrowException() {
        // Arrange
        user.setPassword("Ab1d!sss#defg");

        // Act & Assert
        final ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> userRepository.save(user));

        // Check the exception message
        assertTrue(exception.getMessage().contains("Password must contain lower case letter(s), uppercase letter(s), number(s) and special character(s) (!.*_-)."));
    }
}