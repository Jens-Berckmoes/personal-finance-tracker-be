package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.exception.InvalidUserException;
import be.jensberckmoes.personal_finance_tracker.model.User;
import be.jensberckmoes.personal_finance_tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("testuser", "password123", "test@example.com");

    }

    @Test
    public void givenValidUser_whenRegister_thenUserIsNotNull() {
        // Arrange
        when(userRepository.save(user)).thenReturn(user);
        when(hashingService.hashPassword(user.getPassword())).thenReturn("hashed_password123");
        when(validationService.isValidUsername(user.getUsername())).thenReturn(true);
        when(validationService.isValidEmail(user.getEmail())).thenReturn(true);

        // Act
        final User registeredUser = userService.register(user);

        // Assert
        assertNotNull(registeredUser);
    }

    @Test
    public void givenValidUser_whenRegister_thenUserPropertiesMatch() {
        // Arrange
        when(userRepository.save(user)).thenReturn(user);
        when(hashingService.hashPassword(user.getPassword())).thenReturn("hashed_password123");
        when(validationService.isValidUsername(user.getUsername())).thenReturn(true);
        when(validationService.isValidEmail(user.getEmail())).thenReturn(true);

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
        when(userRepository.save(user)).thenReturn(user);
        when(hashingService.hashPassword(user.getPassword())).thenReturn("hashed_password123");
        when(validationService.isValidUsername(user.getUsername())).thenReturn(true);
        when(validationService.isValidEmail(user.getEmail())).thenReturn(true);

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
        when(userRepository.save(user)).thenReturn(user);
        when(hashingService.hashPassword(user.getPassword())).thenReturn("hashed_password123");
        when(validationService.isValidUsername(user.getUsername())).thenReturn(true);
        when(validationService.isValidEmail(user.getEmail())).thenReturn(true);

        // Act
        final User registeredUser = userService.register(user);

        // Assert
        assertNotNull(registeredUser);
        verify(validationService, times(1)).isValidUsername(user.getUsername());
        verify(validationService, times(1)).isValidEmail(user.getEmail());
    }

}