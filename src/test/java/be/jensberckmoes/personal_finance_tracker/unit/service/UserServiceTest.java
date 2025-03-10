package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.dto.UserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserDto;
import be.jensberckmoes.personal_finance_tracker.exception.InvalidRoleException;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
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
        verify(userRepository).save(userCaptor.capture());
        final User savedUser = userCaptor.getValue();

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
                () -> assertEquals("ADMIN", user.getRole())
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
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        final UserDto userDto = userService.getUserById(1L);

        assertNotNull(userDto);
        assertAll(
                () -> assertEquals("testuser", userDto.getUsername()),
                () -> assertEquals("test@example.com", userDto.getEmail()),
                () -> assertEquals("ADMIN", userDto.getRole())
        );

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void givenInvalidUser_whenFindById_thenThrowException() {
        final InvalidUserException exception = assertThrows(InvalidUserException.class,
                () -> userService.getUserById(99L));

        assertTrue(exception.getMessage().contains("Username does not exist"));
        verify(userRepository, times(1)).findById(99L);
    }

    @ParameterizedTest
    @MethodSource("providedFindByIdValidations")
    public void givenInvalidParameterValues_whenFindById_thenThrowException(final Long idValue,
                                                                            final String expectedMessage) {
        final InvalidUserException exception = assertThrows(InvalidUserException.class,
                () -> userService.getUserById(idValue));

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void whenGetAllUsersIsCalled_thenReturnAllUsers() {
        final User adminuser = User
                .builder()
                .id(1L)
                .password("Ab1!" + "A".repeat(12))
                .username("adminuser")
                .email("adminuser@example.com")
                .role(Role.ADMIN)
                .build();
        final User testuser = User
                .builder()
                .id(1L)
                .password("Ab1!" + "B".repeat(12))
                .username("testuser")
                .email("testuser@example.com")
                .role(Role.USER)
                .build();
        when(userRepository.findAll()).thenReturn(List.of(adminuser, testuser));

        final List<UserDto> userDtoList = userService.getAllUsers();
        assertNotNull(userDtoList);
        assertFalse(userDtoList.isEmpty());
        assertEquals(2, userDtoList.size());
        assertAll(
                () -> assertEquals("adminuser", userDtoList.getFirst().getUsername()),
                () -> assertEquals("testuser", userDtoList.get(1).getUsername())
        );
    }
    @Test
    public void givenUsersExistWithRole_whenFindByRole_thenReturnsUserList() {
        final Role adminRole = Role.ADMIN;
        final Role userRole = Role.USER;
        final User adminuser = User
                .builder()
                .id(1L)
                .password("Ab1!" + "A".repeat(12))
                .username("adminuser")
                .email("adminuser@example.com")
                .role(adminRole)
                .build();
        final User testuser = User
                .builder()
                .id(2L)
                .password("Ab1!" + "B".repeat(12))
                .username("testuser")
                .email("testuser@example.com")
                .role(userRole)
                .build();
        final User testuser1 = User
                .builder()
                .id(3L)
                .password("Ab1!" + "C".repeat(12))
                .username("testuser1")
                .email("testuser1@example.com")
                .role(userRole)
                .build();
        when(userRepository.findByRole(adminRole)).thenReturn(List.of(adminuser));
        when(userRepository.findByRole(userRole)).thenReturn(List.of(testuser, testuser1));

        final List<UserDto> adminDtoList = userService.getUsersByRole(adminRole);
        final List<UserDto> userDtoList = userService.getUsersByRole(userRole);
        assertNotNull(userDtoList);
        assertNotNull(adminDtoList);
        assertFalse(adminDtoList.isEmpty());
        assertFalse(userDtoList.isEmpty());
        assertEquals(1, adminDtoList.size());
        assertEquals(2, userDtoList.size());
        assertAll(
                () -> assertEquals("adminuser", adminDtoList.getFirst().getUsername()),
                () -> assertEquals("ADMIN", adminDtoList.getFirst().getRole()),
                () -> assertEquals("testuser", userDtoList.getFirst().getUsername()),
                () -> assertEquals("USER", userDtoList.getFirst().getRole()),
                () -> assertEquals("testuser1", userDtoList.get(1).getUsername()),
                () -> assertEquals("USER", userDtoList.get(1).getRole())
        );
    }

    @Test
    public void givenNoUsersExistWithRole_whenFindByRole_thenReturnsEmptyList() {
        // Arrange
        final Role role = Role.ADMIN;
        when(userRepository.findByRole(role)).thenReturn(Collections.emptyList());

        // Act
        final List<UserDto> result = userService.getUsersByRole(role);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void givenNullRole_whenFindByRole_thenThrowException() {
        final InvalidRoleException exception = assertThrows(InvalidRoleException.class,
                () -> userService.getUsersByRole(null));

        assertTrue(exception.getMessage().contains("Role cannot be null"));
    }

    @Test
    public void givenNoUsersExist_whenGetAll_thenReturnsEmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        final List<UserDto> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void givenLargeNumberOfUsers_whenGetAll_thenReturnsAllUsers() {
        // Arrange
        final List<User> largeUserList = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            largeUserList.add(User
                    .builder()
                    .id((long) i)
                    .username("user" + i)
                    .password("Ab1!" + "A".repeat(12))
                    .email("user" + i + "@example.com")
                    .role(Role.USER)
                    .build());
        }
        when(userRepository.findAll()).thenReturn(largeUserList);

        final List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1000, result.size());
        assertEquals("user1", result.getFirst().getUsername());
        assertEquals("user1000", result.get(999).getUsername());
    }

    @Test
    public void givenUsersExist_whenGetAll_thenMapsFieldsCorrectly() {
        // Arrange
        final User user = User
                .builder()
                .id(1L)
                .password("Ab1!" + "A".repeat(12))
                .username("adminuser")
                .email("adminuser@example.com")
                .role(Role.ADMIN)
                .build();
        when(userRepository.findAll()).thenReturn(List.of(user));

        final List<UserDto> result = userService.getAllUsers();

        final UserDto userDto = result.getFirst();
        assertEquals("adminuser", userDto.getUsername());
        assertEquals("adminuser@example.com", userDto.getEmail());
        assertEquals("ADMIN", userDto.getRole());
    }
    @Test
    public void givenLargeDataset_whenFindByRole_thenReturnsAllUsers() {
        // Arrange
        final Role role = Role.USER;
        final List<User> users = IntStream
                .rangeClosed(1,1000)
                .mapToObj(i -> new User((long) i, "user" + i, "email" + i + "@example.com", "Password123!", role))
                .toList();

        when(userRepository.findByRole(role)).thenReturn(users);

        // Act
        final List<UserDto> result = userService.getUsersByRole(role);

        // Assert
        assertNotNull(result);
        assertEquals(1000, result.size());
    }

    @Test
    public void whenFindByRole_thenVerifyRepositoryInteraction() {
        // Arrange
        Role role = Role.USER;
        when(userRepository.findByRole(role)).thenReturn(Collections.emptyList());

        // Act
        userService.getUsersByRole(role);

        // Assert
        verify(userRepository, times(1)).findByRole(role);
    }

    private static Stream<Arguments> providedFindByIdValidations() {
        return Stream.of(
                Arguments.of(null, "User ID is invalid"),  // Null value
                Arguments.of(-1L, "User ID is invalid"),   // Negative value
                Arguments.of(0L, "User ID is invalid")     // Boundary value
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