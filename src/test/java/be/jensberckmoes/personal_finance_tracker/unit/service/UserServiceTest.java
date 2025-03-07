package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.dto.UserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserDto;
import be.jensberckmoes.personal_finance_tracker.exception.InvalidUserException;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.model.User;
import be.jensberckmoes.personal_finance_tracker.repository.UserRepository;
import be.jensberckmoes.personal_finance_tracker.service.HashingService;
import be.jensberckmoes.personal_finance_tracker.service.ValidationService;
import be.jensberckmoes.personal_finance_tracker.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private UserCreateDto userCreateDto;
    private User user;

    @BeforeEach
    public void setUp() {
        userCreateDto = UserCreateDto.builder()
                .password("Password123!")
                .username("testuser")
                .email("test@example.com")
                .build();
        user = User.builder()
                .password("Password123!")
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    @Test
    public void givenValidUserCreateDto_whenCreateUser_thenUserIsPersisted() {
        // Arrange
        mockValidUserSetup();
        // Act
        final UserDto createdUser = userService.createUser(userCreateDto);

        assertEquals("testuser", createdUser.getUsername());
        assertEquals("USER", createdUser.getRole());
        assertEquals("test@example.com", createdUser.getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("testuser", savedUser.getUsername());
        assertEquals("hashed_Password123!", savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());
        assertEquals("test@example.com", savedUser.getEmail());

    }


    @Test
    public void givenValidUser_whenCreateUser_thenUserPropertiesMatch() {
        // Arrange
        mockValidUserSetup();

        final UserDto registeredUser = userService.createUser(userCreateDto);

        // Assert
        // Verify that UserDto properties match
        assertEquals(userCreateDto.getUsername(), registeredUser.getUsername());
        assertEquals(userCreateDto.getEmail(), registeredUser.getEmail());

        // Capture the User entity saved to the repository
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // Ensure the password is hashed and not stored in plain text
        assertNotEquals(userCreateDto.getPassword(), savedUser.getPassword()); // Password should be hashed
        verify(hashingService).hashPassword(userCreateDto.getPassword());
    }


    @Test
    public void givenValidUser_whenRegister_thenRepositorySaveIsCalled() {
        // Arrange
        mockValidUserSetup();

        // Act
        final UserDto registeredUser = userService.createUser(userCreateDto);

        // Assert
        assertNotNull(registeredUser);

        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
    }

    @Test
    public void givenNullUser_whenRegister_thenThrowException() {
        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.createUser(null));

        assertEquals("Username is invalid", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Ensure save is not called
    }

    @Test
    public void givenEmptyUsername_whenRegister_thenThrowException() {
        // Arrange
        userCreateDto.setUsername("");

        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.createUser(userCreateDto));

        assertEquals("Username is invalid", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenInvalidEmail_whenRegister_thenThrowException() {
        // Arrange
        userCreateDto.setEmail("invalid-email");
        when(validationService.isValidUsername(userCreateDto.getUsername())).thenReturn(true);
        when(validationService.isValidPassword(userCreateDto.getPassword())).thenReturn(true);
        when(validationService.isValidEmail(userCreateDto.getEmail())).thenReturn(false);

        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.createUser(userCreateDto));

        assertEquals("User has invalid email. Email should be in the form (test@example.com).", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenValidUser_whenRegister_thenValidationServiceMethodsAreCalled() {
        // Arrange
        mockValidUserSetup();

        // Act
        final UserDto registeredUser = userService.createUser(userCreateDto);

        // Assert
        assertNotNull(registeredUser);
        verify(validationService, times(1)).isValidUsername(userCreateDto.getUsername());
        verify(validationService, times(1)).isValidEmail(userCreateDto.getEmail());
    }

    @Test
    public void givenExistingUsername_whenFindByUsername_thenReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(validationService.isValidUsername(userCreateDto.getUsername())).thenReturn(true);

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
        when(userRepository.findByUsername(userCreateDto.getUsername())).thenReturn(Optional.of(user));
        when(validationService.isValidUsername(userCreateDto.getUsername())).thenReturn(true);

        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.createUser(userCreateDto));

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
        userCreateDto.setPassword("123456"); // Weak password
        when(validationService.isValidPassword(userCreateDto.getPassword())).thenReturn(false);
        when(validationService.isValidUsername(userCreateDto.getUsername())).thenReturn(true);

        // Act and Assert
        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.createUser(userCreateDto));

        assertEquals("User has invalid password. Password should be between 12-255 characters long, should contain 1 uppercase, 1 lowercase, 1 number and 1 special character(!.*_-).", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenValidUser_whenCreateUser_thenRawPasswordIsNotPersisted() {
        // Arrange
        mockValidUserSetup();

        // Act
        userService.createUser(userCreateDto);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNotEquals("Password123!", savedUser.getPassword()); // Ensure raw password is not saved
        assertEquals("hashed_Password123!", savedUser.getPassword()); // Ensure the expected hash is saved
    }


    private void mockValidUserSetup() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(hashingService.hashPassword(userCreateDto.getPassword())).thenReturn("hashed_Password123!");
        when(validationService.isValidUsername(userCreateDto.getUsername())).thenReturn(true);
        when(validationService.isValidPassword(userCreateDto.getPassword())).thenReturn(true);
        when(validationService.isValidEmail(userCreateDto.getEmail())).thenReturn(true);
    }
}