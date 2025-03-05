package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.exception.InvalidUserException;
import be.jensberckmoes.personal_finance_tracker.model.User;
import be.jensberckmoes.personal_finance_tracker.repository.UserRepository;
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
        user = new User("testuser", "password123", "test@example.com");

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
        when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));
        when(hashingService.hashPassword(user.getPassword())).thenReturn("hashed_password123");
        when(validationService.isValidUsername(user.getUsername())).thenReturn(true);
        when(validationService.isValidEmail(user.getEmail())).thenReturn(true);

        // Act
        final User registeredUser = userService.register(user);

        // Assert
        assertNotNull(registeredUser);
        assertEquals("hashed_password123", registeredUser.getPassword());
        assertTrue(registeredUser.getPassword().startsWith("hashed_"));
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
        when(validationService.isValidUsername(user.getUsername())).thenReturn(false);

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
        when(validationService.isValidEmail(user.getEmail())).thenReturn(false);

        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.register(user));

        assertEquals("User has invalid email", exception.getMessage());
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
                () -> assertEquals("password123", foundUser.getPassword()),
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
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(validationService.isValidUsername("TestUser")).thenReturn(true);
        // Act
        final Optional<User> possibleUser = userService.findByUsername("TestUser");

        // Assert
        assertTrue(possibleUser.isPresent());
    }

    @Test
    public void givenValidUser_whenRegister_thenPasswordHashingServiceIsCalled() {
        // Arrange
        mockValidUserSetup();

        // Act
        final User registeredUser = userService.register(user);

        // Assert
        verify(hashingService, times(1)).hashPassword("password123");
        assertEquals("hashed_password123", registeredUser.getPassword());
    }

    @Test
    public void givenUserWithWeakPassword_whenRegister_thenThrowException() {
        // Arrange
        user.setPassword("123456"); // Weak password
        when(validationService.isValidPassword(user.getPassword())).thenReturn(false);

        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.register(user));

        assertEquals("User has invalid password. Password should be 8+ characters long, 1 uppercase, 1 lowercase, 1 number and 1 special character(@$!%*?&).", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenValidUser_whenRegister_thenRawPasswordIsNotPersisted() {
        // Arrange
        when(userRepository.save(user)).thenReturn(user);
        when(hashingService.hashPassword(user.getPassword())).thenReturn("hashed_password123");

        // Act
        final User registeredUser = userService.register(user);

        // Assert
        assertNotEquals("password123", registeredUser.getPassword()); // Ensure raw password is not stored
    }

    @Test
    public void givenUsernameWithMixedCase_whenFindByUsername_thenReturnFoundUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(validationService.isValidUsername("TestUser")).thenReturn(true);

        // Act
        final Optional<User> possibleUser = userService.findByUsername("TestUser");

        // Assert
        assertTrue(possibleUser.isPresent());
        verify(userRepository, times(1)).findByUsername("testuser"); // Ensure lookup is case-insensitive
    }

    @Test
    public void givenValidUser_whenRegister_thenSavedUserHasHashedPassword() {
        // Arrange
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(hashingService.hashPassword(user.getPassword())).thenReturn("hashed_password123");

        // Act
        final User registeredUser = userService.register(user);

        // Assert
        assertNotNull(registeredUser);
        assertEquals("hashed_password123", registeredUser.getPassword());
        verify(userRepository).save(argThat(savedUser ->
                "hashed_password123".equals(savedUser.getPassword()) && "testuser".equals(savedUser.getUsername())
        ));
    }

    private void mockValidUserSetup() {
        when(userRepository.save(user)).thenReturn(user);
        when(hashingService.hashPassword(user.getPassword())).thenReturn("hashed_password123");
        when(validationService.isValidUsername(user.getUsername())).thenReturn(true);
        when(validationService.isValidEmail(user.getEmail())).thenReturn(true);
    }
}