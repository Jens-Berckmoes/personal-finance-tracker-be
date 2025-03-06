package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.exception.InvalidUserException;
import be.jensberckmoes.personal_finance_tracker.model.User;
import be.jensberckmoes.personal_finance_tracker.repository.UserRepository;
import be.jensberckmoes.personal_finance_tracker.service.HashingService;
import be.jensberckmoes.personal_finance_tracker.service.ValidationService;
import be.jensberckmoes.personal_finance_tracker.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HashingService hashingService;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private UserServiceImpl userService;

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
    public void givenValidUser_whenRegister_thenUserIsNotNull() {
        // Arrange
        mockValidUserSetup();

        // Act
        final User registeredUser = userService.register(user);

        // Assert
        assertNotNull(registeredUser);
    }

    @Test
    public void givenValidUser_whenRegister_thenUserPropertiesMatch() {
        // Arrange
        mockValidUserSetup();

        // Act
        final User registeredUser = userService.register(user);

        // Assert
        assertEquals(user.getUsername(), registeredUser.getUsername());
        assertEquals(user.getPassword(), registeredUser.getPassword());
        assertEquals(user.getEmail(), registeredUser.getEmail());
    }

    @Test
    public void givenValidUser_whenRegister_thenRepositorySaveIsCalled() {
        // Arrange
        mockValidUserSetup();

        // Act
        final User registeredUser = userService.register(user);

        // Assert
        assertNotNull(registeredUser);
        verify(userRepository, times(1)).save(user); // Verify repository interaction
    }

    @Test
    public void givenValidUser_whenRegister_thenPasswordIsHashed() {
        // Arrange
        mockValidUserSetup();

        // Act
        final User registeredUser = userService.register(user);

        // Assert
        assertNotNull(registeredUser);
        assertTrue(registeredUser.getPassword().startsWith("hashed_"));
        assertEquals("hashed_Password123!", registeredUser.getPassword());
    }

    @Test
    public void givenNullUser_whenRegister_thenThrowException() {
        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.register(null));

        assertEquals("Username is invalid", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Ensure save is not called
    }

    @Test
    public void givenEmptyUsername_whenRegister_thenThrowException() {
        // Arrange
        user.setUsername("");

        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.register(user));

        assertEquals("Username is invalid", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenInvalidEmail_whenRegister_thenThrowException() {
        // Arrange
        user.setEmail("invalid-email");
        when(validationService.isValidUsername(user.getUsername())).thenReturn(true);
        when(validationService.isValidPassword(user.getPassword())).thenReturn(true);
        when(validationService.isValidEmail(user.getEmail())).thenReturn(false);

        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.register(user));

        assertEquals("User has invalid email. Email should be in the form (test@example.com).", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenValidUser_whenRegister_thenValidationServiceMethodsAreCalled() {
        // Arrange
        mockValidUserSetup();

        // Act
        final User registeredUser = userService.register(user);

        // Assert
        assertNotNull(registeredUser);
        verify(validationService, times(1)).isValidUsername(user.getUsername());
        verify(validationService, times(1)).isValidEmail(user.getEmail());
    }

    @Test
    public void givenExistingUsername_whenFindByUsername_thenReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(validationService.isValidUsername(user.getUsername())).thenReturn(true);

        // Act
        final Optional<User> possibleUser = userService.findByUsername("testuser");

        // Assert
        assertNotNull(possibleUser);
        assertTrue(possibleUser.isPresent());
        final User foundUser = possibleUser.get();
        assertAll(
                () -> assertEquals("testuser", foundUser.getUsername()),
                () -> assertEquals("Password123!", foundUser.getPassword()),
                () -> assertEquals("test@example.com", foundUser.getEmail())
        );
        verify(userRepository, times(1)).findByUsername("testuser");
    }


    @Test
    public void givenNonExistingUsername_whenFindByUsername_thenReturnEmptyOptional() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(validationService.isValidUsername("nonexistent")).thenReturn(true);

        // Act
        Optional<User> possibleUser = userService.findByUsername("nonexistent");

        // Assert
        assertNotNull(possibleUser);
        assertFalse(possibleUser.isPresent()); // Assert that the Optional is empty
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    public void givenNullUsername_whenFindByUsername_thenThrowException() {
        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.findByUsername(null));

        assertTrue(exception.getMessage().contains("invalid"), "Message should indicate invalid input.");
        verify(userRepository, never()).findByUsername(anyString());
    }


    @Test
    public void givenEmptyUsername_whenFindByUsername_thenThrowException() {
        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.findByUsername(""));

        assertTrue(exception.getMessage().contains("invalid"), "Message should indicate invalid input.");
        verify(userRepository, never()).findByUsername(anyString());
    }


    @Test
    public void givenExistingUsername_whenRegister_thenThrowException() {
        // Arrange
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(validationService.isValidUsername(user.getUsername())).thenReturn(true);

        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.register(user));

        assertEquals("Username already taken", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenUsernameInDifferentCase_whenFindByUsername_thenReturnFoundUser() {
        // Arrange
        when(userRepository.findByUsername("TestUser")).thenReturn(Optional.of(user));
        when(validationService.isValidUsername("testuser")).thenReturn(true);
        // Act
        final Optional<User> possibleUser = userService.findByUsername("TestUser");

        // Assert
        assertTrue(possibleUser.isPresent());
        verify(userRepository, times(1)).findByUsername("TestUser");
    }

    @Test
    public void givenUserWithWeakPassword_whenRegister_thenThrowException() {
        // Arrange
        user.setPassword("123456"); // Weak password
        when(validationService.isValidPassword(user.getPassword())).thenReturn(false);
        when(validationService.isValidUsername(user.getUsername())).thenReturn(true);

        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.register(user));

        assertEquals("User has invalid password. Password should be between 12-64 characters long, should contain 1 uppercase, 1 lowercase, 1 number and 1 special character(!.*_-).", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenValidUser_whenRegister_thenRawPasswordIsNotPersisted() {
        // Arrange
        mockValidUserSetup();

        // Act
        final User registeredUser = userService.register(user);

        // Assert
        assertNotEquals("Password123!", registeredUser.getPassword()); // Ensure raw password is not stored
    }

    private void mockValidUserSetup() {
        when(userRepository.save(user)).thenReturn(user);
        when(hashingService.hashPassword(user.getPassword())).thenReturn("hashed_Password123!");
        when(validationService.isValidUsername(user.getUsername())).thenReturn(true);
        when(validationService.isValidPassword(user.getPassword())).thenReturn(true);
        when(validationService.isValidEmail(user.getEmail())).thenReturn(true);
    }
}