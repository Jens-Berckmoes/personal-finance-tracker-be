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
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

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
    private UserDto userDto;

    @BeforeEach
    public void setUp() {
        userCreateDto = UserCreateDto
                .builder()
                .password("Password123!")
                .username("testuser")
                .email("test@example.com")
                .build();
        user = User
                .builder()
                .id(1L)
                .password("Password123!")
                .username("testuser")
                .email("test@example.com")
                .role(Role.ADMIN)
                .build();
        userDto = UserDto
                .builder()
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER.toString())
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

        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        final User savedUser = userCaptor.getValue();
        verify(userRepository).save(userCaptor.capture());

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
        final User savedUser = userCaptor.getValue();

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
        final UserDto user = userService.findByUsername("testuser");

        // Assert
        assertNotNull(user);
        assertAll(
                () -> assertEquals("testuser", user.getUsername()),
                () -> assertEquals("test@example.com", user.getEmail()),
                () -> assertEquals("USER", user.getRole())
        );
        verify(userRepository, times(1)).findByUsername("testuser");
    }


    @Test
    public void givenNonExistingUsername_whenFindByUsername_thenThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(validationService.isValidUsername("nonexistent")).thenReturn(true);

        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.findByUsername("nonexistent"));

        assertTrue(exception.getMessage().contains("Username does not exist"));
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
        final UserDto userDto = userService.findByUsername("TestUser");

        // Assert
        assertNotNull(userDto);
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
        mockValidUserSetup();

        userService.createUser(userCreateDto);

        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        final User savedUser = userCaptor.getValue();

        assertNotEquals("Password123!", savedUser.getPassword()); // Ensure raw password is not saved
        assertEquals("hashed_Password123!", savedUser.getPassword()); // Ensure the expected hash is saved
    }

    @Test
    public void givenValidUser_whenFindById_thenUserIsReturned() {
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));

        final UserDto returnedUserDto = userService.findUserById(1L);

        assertNotNull(returnedUserDto);
        assertAll(
                () -> assertEquals("testuser", returnedUserDto.getUsername()),
                () -> assertEquals("test@example.com", returnedUserDto.getEmail()),
                () -> assertEquals("ADMIN", returnedUserDto.getRole())
        );

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void givenInvalidUser_whenFindById_thenThrowException() {
        final InvalidUserException exception = assertThrows(InvalidUserException.class,
                () -> userService.findUserById(99L));

        assertTrue(exception.getMessage().contains("Username does not exist"));
    }

    @ParameterizedTest
    @MethodSource("providedFindByIdValidations")
    public void givenInvalidParameterValues_whenFindById_thenThrowException(final Long idValue,
                                                                            final String expectedMessage) {
        final InvalidUserException exception = assertThrows(InvalidUserException.class,
                () -> userService.findUserById(idValue));

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    private static Stream<Arguments> providedFindByIdValidations() {
        return Stream.of(
                Arguments.of(null, "Username is invalid"),  // null value
                Arguments.of(-1L, "Username is invalid")    // negative value
        );
    }

    private void mockValidUserSetup() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(hashingService.hashPassword(userCreateDto.getPassword())).thenReturn("hashed_Password123!");
        when(validationService.isValidUsername(userCreateDto.getUsername())).thenReturn(true);
        when(validationService.isValidPassword(userCreateDto.getPassword())).thenReturn(true);
        when(validationService.isValidEmail(userCreateDto.getEmail())).thenReturn(true);
    }
}