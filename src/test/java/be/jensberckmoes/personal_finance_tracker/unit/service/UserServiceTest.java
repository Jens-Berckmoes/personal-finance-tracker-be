package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.dto.UserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserUpdateDto;
import be.jensberckmoes.personal_finance_tracker.exception.*;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.model.User;
import be.jensberckmoes.personal_finance_tracker.model.UserEntityMapper;
import be.jensberckmoes.personal_finance_tracker.repository.UserRepository;
import be.jensberckmoes.personal_finance_tracker.service.HashingService;
import be.jensberckmoes.personal_finance_tracker.service.UserServiceImpl;
import be.jensberckmoes.personal_finance_tracker.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Spy
    private UserEntityMapper userEntityMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserCreateDto userCreateDto;
    private User user;

    @BeforeEach
    public void setUp() {
        userCreateDto = UserCreateDto
                .builder()
                .username("testuser")
                .password("Password123!")
                .email("test@example.com")
                .build();
        user = User
                .builder()
                .id(1L)
                .username("testuser")
                .password("Password123!")
                .email("test@example.com")
                .role(Role.ADMIN)
                .build();
        user = createTestUser(1L, "testuser", "Password123!", "test@example.com", Role.ADMIN);
    }

    @Test
    public void givenValidUserCreateDto_whenCreateUser_thenUserIsPersisted() {
        mockValidUserSetup();

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
        mockValidUserSetup();

        final UserDto registeredUser = userService.createUser(userCreateDto);

        assertEquals(userCreateDto.getUsername(), registeredUser.getUsername());
        assertEquals(userCreateDto.getEmail(), registeredUser.getEmail());

        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        final User savedUser = userCaptor.getValue();

        assertNotEquals(userCreateDto.getPassword(), savedUser.getPassword());
        verify(hashingService).hashPassword(userCreateDto.getPassword());
    }


    @Test
    public void givenValidUser_whenRegister_thenRepositorySaveIsCalled() {
        mockValidUserSetup();

        final UserDto registeredUser = userService.createUser(userCreateDto);

        assertNotNull(registeredUser);

        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
    }

    @Test
    public void givenNullUser_whenRegister_thenThrowException() {
        final Exception exception = assertThrows(NullParameterException.class, () -> userService.createUser(null));

        assertEquals("Parameter 'userUpdateDto' cannot be null", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Ensure save is not called
    }

    @Test
    public void givenEmptyUsername_whenRegister_thenThrowException() {
        userCreateDto.setUsername("");

        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.createUser(userCreateDto));

        assertEquals("Username is invalid", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenInvalidEmail_whenRegister_thenThrowException() {
        userCreateDto.setEmail("invalid-email");
        when(validationService.isValidUsername(userCreateDto.getUsername())).thenReturn(true);
        when(validationService.isValidPassword(userCreateDto.getPassword())).thenReturn(true);
        when(validationService.isValidEmail(userCreateDto.getEmail())).thenReturn(false);

        final Exception exception = assertThrows(InvalidEmailException.class, () -> userService.createUser(userCreateDto));

        assertEquals("User has invalid email. Email should be in the form (test@example.com).", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenValidUser_whenRegister_thenValidationServiceMethodsAreCalled() {
        mockValidUserSetup();

        final UserDto registeredUser = userService.createUser(userCreateDto);

        assertNotNull(registeredUser);
        verify(validationService, times(1)).isValidUsername(userCreateDto.getUsername());
        verify(validationService, times(1)).isValidEmail(userCreateDto.getEmail());
    }

    @Test
    public void givenExistingUsername_whenFindByUsername_thenReturnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(validationService.isValidUsername(userCreateDto.getUsername())).thenReturn(true);

        final UserDto user = userService.findByUsername("testuser");

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
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(validationService.isValidUsername("nonexistent")).thenReturn(true);

        final Exception exception = assertThrows(InvalidUserException.class, () -> userService.findByUsername("nonexistent"));

        assertTrue(exception.getMessage().contains("Username does not exist"));
    }

    @Test
    public void givenNullUsername_whenFindByUsername_thenThrowException() {
        final Exception exception = assertThrows(NullParameterException.class, () -> userService.findByUsername(null));

        assertTrue(exception.getMessage().contains("Parameter 'username' cannot be null"), "Message should indicate invalid input.");
        verify(userRepository, never()).findByUsername(anyString());
    }


    @Test
    public void givenEmptyUsername_whenFindByUsername_thenThrowException() {
        final Exception exception = assertThrows(BlankParameterException.class, () -> userService.findByUsername(""));

        assertTrue(exception.getMessage().contains("Parameter 'username' cannot be blank"), "Message should indicate invalid input.");
        verify(userRepository, never()).findByUsername(anyString());
    }


    @Test
    public void givenExistingUsername_whenRegister_thenThrowException() {
        when(userRepository.existsByUsername(userCreateDto.getUsername())).thenReturn(true);

        final Exception exception = assertThrows(DuplicateUsernameException.class, () -> userService.createUser(userCreateDto));

        assertEquals("Username already taken", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenUsernameInDifferentCase_whenFindByUsername_thenReturnFoundUser() {
        when(userRepository.findByUsername("TestUser")).thenReturn(Optional.of(user));
        when(validationService.isValidUsername("testuser")).thenReturn(true);

        final UserDto userDto = userService.findByUsername("TestUser");

        assertNotNull(userDto);
        verify(userRepository, times(1)).findByUsername("TestUser");
    }

    @Test
    public void givenUserWithWeakPassword_whenRegister_thenThrowException() {
        userCreateDto.setPassword("123456");
        when(validationService.isValidPassword(userCreateDto.getPassword())).thenReturn(false);
        when(validationService.isValidUsername(userCreateDto.getUsername())).thenReturn(true);

        final Exception exception = assertThrows(InvalidPasswordException.class, () -> userService.createUser(userCreateDto));

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
        final InvalidUserIDException exception = assertThrows(InvalidUserIDException.class,
                () -> userService.getUserById(idValue));

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void whenGetAllUsersIsCalled_thenReturnAllUsers() {
        final User adminUser = createTestUser(1L, "adminuser", "Ab1!" + "A".repeat(12), "adminuser@example.com", Role.ADMIN);
        final User testUser = createTestUser(2L, "testuser", "Ab1!" + "B".repeat(12), "testuser@example.com", Role.USER);
        when(userRepository.findAll()).thenReturn(List.of(adminUser, testUser));

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
        final User adminUser = createTestUser(1L, "adminuser", "Ab1!" + "A".repeat(12), "adminuser@example.com", Role.ADMIN);
        final User testUser = createTestUser(2L, "testuser", "Ab1!" + "B".repeat(12), "testuser@example.com", Role.USER);
        final User testUser1 = createTestUser(3L, "testuser1", "Ab1!" + "C".repeat(12), "testuser1@example.com", Role.USER);

        when(userRepository.findByRole(adminRole)).thenReturn(List.of(adminUser));
        when(userRepository.findByRole(userRole)).thenReturn(List.of(testUser, testUser1));

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
        final Role role = Role.ADMIN;
        when(userRepository.findByRole(role)).thenReturn(Collections.emptyList());

        final List<UserDto> result = userService.getUsersByRole(role);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void givenNullRole_whenFindByRole_thenThrowException() {
        final NullParameterException exception = assertThrows(NullParameterException.class,
                () -> userService.getUsersByRole(null));

        assertTrue(exception.getMessage().contains("Role cannot be null"));
    }

    @Test
    public void givenNoUsersExist_whenGetAll_thenReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        final List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void givenLargeNumberOfUsers_whenGetAll_thenReturnsAllUsers() {
        final List<User> largeUserList = IntStream
                .rangeClosed(1, 1000)
                .mapToObj(i -> new User((long) i, "user" + i, "email" + i + "@example.com", "Password123!", Role.USER))
                .toList();

        when(userRepository.findAll()).thenReturn(largeUserList);

        final List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1000, result.size());
        assertEquals("user1", result.getFirst().getUsername());
        assertEquals("user1000", result.get(999).getUsername());
    }

    @Test
    public void givenUsersExist_whenGetAll_thenMapsFieldsCorrectly() {
        final User adminUser = createTestUser(1L, "adminuser", "Ab1!" + "A".repeat(12), "adminuser@example.com", Role.ADMIN);
        when(userRepository.findAll()).thenReturn(List.of(adminUser));

        final List<UserDto> result = userService.getAllUsers();

        final UserDto userDto = result.getFirst();
        assertEquals("adminuser", userDto.getUsername());
        assertEquals("adminuser@example.com", userDto.getEmail());
        assertEquals("ADMIN", userDto.getRole());
    }

    @Test
    public void givenLargeDataset_whenFindByRole_thenReturnsAllUsers() {
        final Role role = Role.USER;
        final List<User> users = IntStream
                .rangeClosed(1, 1000)
                .mapToObj(i -> new User((long) i, "user" + i, "email" + i + "@example.com", "Password123!", role))
                .toList();

        when(userRepository.findByRole(role)).thenReturn(users);

        final List<UserDto> result = userService.getUsersByRole(role);

        assertNotNull(result);
        assertEquals(1000, result.size());
    }

    @Test
    public void whenFindByRole_thenVerifyRepositoryInteraction() {
        final Role role = Role.USER;
        when(userRepository.findByRole(role)).thenReturn(Collections.emptyList());

        userService.getUsersByRole(role);

        verify(userRepository, times(1)).findByRole(role);
    }

    @Test
    public void givenMatchingSubstring_whenGetUsersByUsernameContains_thenReturnsUserList() {
        final String substring = "test";
        final Pageable pageable = PageRequest.of(0, 2);
        final List<User> users = List.of(
                createTestUser(1L, "testuser1", "Password123!", "email1@example.com", Role.USER),
                createTestUser(2L, "anothertestuser", "Password123!", "email2@example.com", Role.USER)
        );
        final Page<User> userPage = new PageImpl<>(users, pageable, 5);
        when(userRepository.findByUsernameContaining(substring, pageable)).thenReturn(userPage);

        final Page<UserDto> result = userService.getUsersByUsernameContains(substring, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals("testuser1", result.getContent().getFirst().getUsername());
        assertEquals("anothertestuser", result.getContent().get(1).getUsername());
    }

    @Test
    public void givenNoMatchingSubstring_whenGetUsersByUsernameContains_thenReturnsEmptyPage() {
        final String substring = "nomatch";
        final Pageable pageable = PageRequest.of(0, 2);
        final Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(userRepository.findByUsernameContaining(substring, pageable)).thenReturn(emptyPage);

        final Page<UserDto> result = userService.getUsersByUsernameContains(substring, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }

    @Test
    public void givenCaseInsensitiveMatch_whenGetUsersByUsernameContains_thenReturnsUsers() {
        final String substring = "Test";
        final Pageable pageable = PageRequest.of(0, 2);
        final List<User> users = List.of(
                createTestUser(1L, "testuser1", "Password123!", "email1@example.com", Role.USER),
                createTestUser(2L, "TestUser2", "Password123!", "email1@example.com", Role.USER)
        );
        final Page<User> userPage = new PageImpl<>(users, pageable, 5);
        when(userRepository.findByUsernameContaining(substring, pageable)).thenReturn(userPage);

        final Page<UserDto> result = userService.getUsersByUsernameContains(substring, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals("testuser1", result.getContent().getFirst().getUsername());
        assertEquals("TestUser2", result.getContent().get(1).getUsername());
    }

    @Test
    public void givenEmptySubstring_whenFindByUsernameContainsWithPagination_thenReturnsAllUsersPaginated() {
        // Arrange
        final String substring = "";
        final Pageable pageable = PageRequest.of(0, 2); // Page 0, 2 results per page
        final List<User> users = List.of(
                createTestUser(1L, "user1", "Password123!", "email1@example.com", Role.USER),
                createTestUser(2L, "user2", "Password123!", "email2@example.com", Role.USER)
        );
        final Page<User> userPage = new PageImpl<>(users, pageable, 5);

        when(userRepository.findByUsernameContaining(substring, pageable)).thenReturn(userPage);

        // Act
        final Page<UserDto> result = userService.getUsersByUsernameContains(substring, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals("user1", result.getContent().getFirst().getUsername());
        assertEquals("user2", result.getContent().get(1).getUsername());
    }

    @Test
    public void givenNullId_whenUpdateDto_thenThrowException() {
        final UserUpdateDto userUpdateDto = new UserUpdateDto("updatedUsername", "updated@example.com");
        final Exception exception = assertThrows(NullParameterException.class, () -> userService.updateUser(null, userUpdateDto));

        assertEquals("Parameters 'id' and 'userUpdateDto' cannot be null", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Ensure save is not called
    }

    @Test
    public void givenNullUserUpdateDto_whenUpdateDto_thenThrowException() {
        final Exception exception = assertThrows(NullParameterException.class, () -> userService.updateUser(1L, null));

        assertEquals("Parameters 'id' and 'userUpdateDto' cannot be null", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Ensure save is not called
    }

    @Test
    public void givenValidIdAndUserUpdateDto_whenUpdateUser_thenReturnUpdatedUserDto() {
        final Long id = 1L;
        final UserUpdateDto userUpdateDto = new UserUpdateDto("updatedUsername", "updated@example.com");
        final User existingUser = createTestUser(id, "oldUsername", "Password123!", "old@example.com", Role.USER);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(validationService.isValidEmail(userUpdateDto.getEmail())).thenReturn(true);

        final UserDto result = userService.updateUser(id, userUpdateDto);

        assertNotNull(result);
        assertEquals("updatedUsername", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    public void givenNonExistingId_whenUpdateUser_thenThrowUserNotFoundException() {
        final Long id = 1L;
        final UserUpdateDto userUpdateDto = new UserUpdateDto("updatedUsername", "updated@example.com");
        when(validationService.isValidEmail(userUpdateDto.getEmail())).thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(id, userUpdateDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenInvalidUserUpdateDto_whenUpdateUser_thenThrowValidationException() {
        final Long id = 1L;
        final UserUpdateDto userUpdateDto = new UserUpdateDto(null, "invalid-email");

        assertThrows(InvalidEmailException.class, () -> userService.updateUser(id, userUpdateDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenPartialUserUpdateDto_whenUpdateUser_thenOnlyUpdateNonNullFields() {
        final Long id = 1L;
        final UserUpdateDto userUpdateDto = new UserUpdateDto(null, "updated@example.com");
        final User existingUser = createTestUser(id, "oldUsername", "Password123!", "old@example.com", Role.USER);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(validationService.isValidEmail(userUpdateDto.getEmail())).thenReturn(true);

        final UserDto result = userService.updateUser(id, userUpdateDto);

        assertNotNull(result);
        assertEquals("oldUsername", result.getUsername()); // Username not updated
        assertEquals("updated@example.com", result.getEmail()); // Email updated
    }

    @Test
    public void whenUpdateUser_thenVerifyRepositoryInteractions() {
        // Arrange
        final Long id = 1L;
        final UserUpdateDto userUpdateDto = new UserUpdateDto("updatedUsername", "updated@example.com");
        final User existingUser = createTestUser(id, "oldUsername", "Password123!", "old@example.com", Role.USER);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(validationService.isValidEmail(userUpdateDto.getEmail())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(id, userUpdateDto);

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void givenEmailAlreadyExists_whenUpdateUser_thenThrowDuplicateEmailException() {
        // Arrange
        final Long id = 1L;
        final UserUpdateDto userUpdateDto = new UserUpdateDto("updatedUsername", "existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        when(validationService.isValidEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> userService.updateUser(id, userUpdateDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenValidId_whenDeleteUser_thenUserIsDeleted() {
        final Long id = 1L;
        doNothing().when(userRepository).deleteById(id);

        userService.deleteUser(id);

        verify(userRepository, times(1)).deleteById(id);
    }

    @Test
    public void givenNullId_whenDeleteUser_thenThrowNullParameterException() {
        assertThrows(InvalidUserIDException.class, () -> userService.deleteUser(null));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    public void givenNonExistentId_whenDeleteUser_thenDoNothing() {
        final Long id = 99L;
        doNothing().when(userRepository).deleteById(id);

        userService.deleteUser(id);

        verify(userRepository, times(1)).deleteById(id);
    }

    @Test
    public void givenExistingUsername_whenUsernameExists_thenReturnTrue() {
        final String existingUsername = "testuser";
        when(userRepository.existsByUsername(existingUsername)).thenReturn(true);

        final boolean result = userService.usernameExists(existingUsername);

        assertTrue(result);
        verify(userRepository, times(1)).existsByUsername(existingUsername);
    }

    @Test
    public void givenNonExistingUsername_whenUsernameExists_thenReturnFalse() {
        final String nonExistingUsername = "nonexistentuser";
        when(userRepository.existsByUsername(nonExistingUsername)).thenReturn(false);

        // Act
        final boolean result = userService.usernameExists(nonExistingUsername);

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).existsByUsername(nonExistingUsername);
    }

    @Test
    public void givenNullUsername_whenUsernameExists_thenThrowNullParameterException() {
        assertThrows(NullParameterException.class, () -> userService.usernameExists(null));
        verify(userRepository, never()).existsByUsername(any());
    }

    @Test
    public void givenBlankUsername_whenUsernameExists_thenThrowBlankParameterException() {
        assertThrows(BlankParameterException.class, () -> userService.usernameExists("   "));
        verify(userRepository, never()).existsByUsername(any());
    }

    @Test
    public void givenUsernameInDifferentCase_whenUsernameExists_thenReturnCorrectResult() {
        final String username = "TestUser";
        when(userRepository.existsByUsername("TestUser")).thenReturn(true);

        final boolean result = userService.usernameExists(username);

        assertTrue(result);
        verify(userRepository, times(1)).existsByUsername(username);
    }

    @Test
    public void givenExistingEmail_whenEmailExists_thenReturnTrue() {
        final String existingEmail = "test@example.com";
        when(userRepository.existsByEmail(existingEmail)).thenReturn(true);

        final boolean result = userService.emailExists(existingEmail);

        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail(existingEmail);
    }

    @Test
    public void givenNonExistingEmail_whenEmailExists_thenReturnFalse() {
        final String nonExistingEmail = "nonexistent@example.com";
        when(userRepository.existsByEmail(nonExistingEmail)).thenReturn(false);

        final boolean result = userService.emailExists(nonExistingEmail);

        assertFalse(result);
        verify(userRepository, times(1)).existsByEmail(nonExistingEmail);
    }

    @Test
    public void givenNullEmail_whenEmailExists_thenThrowNullParameterException() {
        assertThrows(NullParameterException.class, () -> userService.emailExists(null));
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    public void givenBlankEmail_whenEmailExists_thenThrowBlankParameterException() {
        assertThrows(BlankParameterException.class, () -> userService.emailExists("   "));
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    public void givenEmailInDifferentCase_whenEmailExists_thenReturnCorrectResult() {
        final String email = "Test@Example.com";
        when(userRepository.existsByEmail("Test@Example.com")).thenReturn(true);

        final boolean result = userService.emailExists(email);

        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    public void givenValidUserIdAndRole_whenHasRole_thenReturnTrue() {
        final Long userId = 1L;
        final Role role = Role.ADMIN;
        when(userRepository.findByIdAndRole(userId, role)).thenReturn(true);

        final boolean result = userService.hasRole(userId, role);

        assertTrue(result);
        verify(userRepository, times(1)).findByIdAndRole(userId, role);
    }

    @Test
    public void givenUserIdAndRoleNotMatching_whenHasRole_thenReturnFalse() {
        final Long userId = 1L;
        final Role role = Role.USER;
        when(userRepository.findByIdAndRole(userId, role)).thenReturn(false);

        final boolean result = userService.hasRole(userId, role);

        assertFalse(result);
        verify(userRepository, times(1)).findByIdAndRole(userId, role);
    }

    @Test
    public void givenNullUserId_whenHasRole_thenThrowNullParameterException() {
        final Role role = Role.ADMIN;

        assertThrows(InvalidUserIDException.class, () -> userService.hasRole(null, role));
        verify(userRepository, never()).findByIdAndRole(any(), any());
    }

    @Test
    public void givenNegativeUserId_whenHasRole_thenThrowInvalidUserIDException() {
        final Long userId = -1L;
        final Role role = Role.ADMIN;

        assertThrows(InvalidUserIDException.class, () -> userService.hasRole(userId, role));
        verify(userRepository, never()).findByIdAndRole(any(), any());
    }

    @Test
    public void givenZeroUserId_whenHasRole_thenThrowInvalidUserIDException() {
        final Long userId = 0L;
        final Role role = Role.ADMIN;

        assertThrows(InvalidUserIDException.class, () -> userService.hasRole(userId, role));
        verify(userRepository, never()).findByIdAndRole(any(), any());
    }

    @Test
    public void givenNullRole_whenHasRole_thenThrowNullParameterException() {
        final Long userId = 1L;

        assertThrows(NullParameterException.class, () -> userService.hasRole(userId, null));
        verify(userRepository, never()).findByIdAndRole(any(), any());
    }

    private static Stream<Arguments> providedFindByIdValidations() {
        return Stream.of(
                Arguments.of(null, "ID must be a positive number"),  // Null value
                Arguments.of(-1L, "ID must be a positive number"),   // Negative value
                Arguments.of(0L, "ID must be a positive number")     // Boundary value
        );
    }

    private static User createTestUser(final Long id, final String username, final String password, final String email, final Role role) {
        return User.builder().id(id).username(username).password(password).email(email).role(role).build();
    }

    private void mockValidUserSetup() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(hashingService.hashPassword(userCreateDto.getPassword())).thenReturn("hashed_Password123!");
        when(validationService.isValidUsername(userCreateDto.getUsername())).thenReturn(true);
        when(validationService.isValidPassword(userCreateDto.getPassword())).thenReturn(true);
        when(validationService.isValidEmail(userCreateDto.getEmail())).thenReturn(true);
    }
}