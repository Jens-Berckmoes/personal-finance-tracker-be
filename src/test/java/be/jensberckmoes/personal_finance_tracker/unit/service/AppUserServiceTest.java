package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.dto.AppUserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserUpdateDto;
import be.jensberckmoes.personal_finance_tracker.exception.*;
import be.jensberckmoes.personal_finance_tracker.model.AppUser;
import be.jensberckmoes.personal_finance_tracker.model.AppUserEntityMapper;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.repository.AppUserRepository;
import be.jensberckmoes.personal_finance_tracker.service.AppUserValidationService;
import be.jensberckmoes.personal_finance_tracker.service.impl.AppUserServiceImpl;
import be.jensberckmoes.personal_finance_tracker.service.HashingService;
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
public class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private HashingService hashingService;

    @Mock
    private AppUserValidationService validationService;

    @Spy
    private AppUserEntityMapper appUserEntityMapper;

    @InjectMocks
    private AppUserServiceImpl userService;

    private AppUserCreateDto appUserCreateDto;
    private AppUser appUser;

    @BeforeEach
    public void setUp() {
        appUserCreateDto = AppUserCreateDto
                .builder()
                .username("testuser")
                .password("Password123!")
                .email("test@example.com")
                .build();
        appUser = AppUser
                .builder()
                .id(1L)
                .username("testuser")
                .password("Password123!")
                .email("test@example.com")
                .role(Role.ADMIN)
                .build();
        appUser = createTestUser(1L, "testuser", "Password123!", "test@example.com", Role.ADMIN);
    }

    @Test
    public void givenValidUserCreateDto_whenCreateUser_thenUserIsPersisted() {
        mockValidUserSetup();

        final AppUserDto createdUser = userService.createUser(appUserCreateDto);

        assertEquals("testuser", createdUser.getUsername());
        assertEquals("USER", createdUser.getRole());
        assertEquals("test@example.com", createdUser.getEmail());

        final ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());
        final AppUser savedAppUser = userCaptor.getValue();

        assertEquals("testuser", savedAppUser.getUsername());
        assertEquals("hashed_Password123!", savedAppUser.getPassword());
        assertEquals("test@example.com", savedAppUser.getEmail());

    }

    @Test
    public void givenValidUser_whenCreateUser_thenUserPropertiesMatch() {
        mockValidUserSetup();

        final AppUserDto registeredUser = userService.createUser(appUserCreateDto);

        assertEquals(appUserCreateDto.getUsername(), registeredUser.getUsername());
        assertEquals(appUserCreateDto.getEmail(), registeredUser.getEmail());

        final ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());
        final AppUser savedAppUser = userCaptor.getValue();

        assertNotEquals(appUserCreateDto.getPassword(), savedAppUser.getPassword());
        verify(hashingService).hashPassword(appUserCreateDto.getPassword());
    }

    @Test
    public void givenValidUser_whenRegister_thenRepositorySaveIsCalled() {
        mockValidUserSetup();

        final AppUserDto registeredUser = userService.createUser(appUserCreateDto);

        assertNotNull(registeredUser);

        final ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository, times(1)).save(userCaptor.capture());
    }

    @Test
    public void givenNullUser_whenRegister_thenThrowException() {
        doThrow(new NullParameterException("Parameter 'userUpdateDto' cannot be null"))
                .when(validationService).validateAppUserCreateDto(any());

        final Exception exception = assertThrows(NullParameterException.class, () -> userService.createUser(null));

        assertEquals("Parameter 'userUpdateDto' cannot be null", exception.getMessage());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    public void givenEmptyUsername_whenRegister_thenThrowException() {
        appUserCreateDto.setUsername("");
        doThrow(new InvalidAppUserNameException("Username is invalid"))
                .when(validationService).validateAppUserCreateDto(appUserCreateDto);

        final Exception exception = assertThrows(InvalidAppUserNameException.class, () -> userService.createUser(appUserCreateDto));

        assertEquals("Username is invalid", exception.getMessage());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    public void givenInvalidEmail_whenRegister_thenThrowException() {
        appUserCreateDto.setEmail("invalid-email");

        doThrow(new InvalidEmailException("User has invalid email. Email should be in the form (test@example.com)."))
                .when(validationService)
                .validateAppUserCreateDto(appUserCreateDto);

        final Exception exception = assertThrows(InvalidEmailException.class, () -> userService.createUser(appUserCreateDto));

        assertEquals("User has invalid email. Email should be in the form (test@example.com).", exception.getMessage());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    public void givenValidUser_whenRegister_thenValidationServiceMethodIsCalled() {
        mockValidUserSetup();

        final AppUserDto registeredUser = userService.createUser(appUserCreateDto);

        assertNotNull(registeredUser);
        verify(validationService, times(1)).validateAppUserCreateDto(appUserCreateDto);
    }

    @Test
    public void givenExistingUsername_whenFindByUsername_thenReturnUser() {
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(appUser));

        final AppUserDto user = userService.findByUsername("testuser");

        assertNotNull(user);
        assertAll(
                () -> assertEquals("testuser", user.getUsername()),
                () -> assertEquals("test@example.com", user.getEmail()),
                () -> assertEquals("ADMIN", user.getRole())
        );
        verify(appUserRepository, times(1)).findByUsername("testuser");
    }


    @Test
    public void givenNonExistingUsername_whenFindByUsername_thenThrowException() {
        when(appUserRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        final Exception exception = assertThrows(InvalidAppUserNameException.class, () -> userService.findByUsername("nonexistent"));

        assertTrue(exception.getMessage().contains("Username does not exist"));
    }

    @Test
    public void givenNullUsername_whenFindByUsername_thenThrowException() {
        doThrow(new InvalidAppUserNameException("Username can not be null."))
                .when(validationService).validateUsername(null);
        final Exception exception = assertThrows(InvalidAppUserNameException.class, () -> userService.findByUsername(null));

        assertTrue(exception.getMessage().contains("Username can not be null."), "Message should indicate invalid input.");
        
        verify(appUserRepository, never()).findByUsername(anyString());
    }


    @Test
    public void givenEmptyUsername_whenFindByUsername_thenThrowException() {
        doThrow(new InvalidAppUserNameException("Username can not be empty"))
                .when(validationService).validateUsername(any());
        final Exception exception = assertThrows(InvalidAppUserNameException.class, () -> userService.findByUsername(""));

        assertTrue(exception.getMessage().contains("Username can not be empty"), "Message should indicate invalid input.");
        verify(appUserRepository, never()).findByUsername(anyString());
    }


    @Test
    public void givenExistingUsername_whenRegister_thenThrowException() {
        when(appUserRepository.existsByUsername(appUserCreateDto.getUsername())).thenReturn(true);

        final Exception exception = assertThrows(DuplicateUsernameException.class, () -> userService.createUser(appUserCreateDto));

        assertEquals("Username already taken", exception.getMessage());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    public void givenUsernameInDifferentCase_whenFindByUsername_thenReturnFoundUser() {
        when(appUserRepository.findByUsername("TestUser")).thenReturn(Optional.of(appUser));

        final AppUserDto appUserDto = userService.findByUsername("TestUser");

        assertNotNull(appUserDto);
        verify(appUserRepository, times(1)).findByUsername("TestUser");
    }

    @Test
    public void givenUserWithWeakPassword_whenRegister_thenThrowException() {
        appUserCreateDto.setPassword("123456");

        doThrow(new InvalidPasswordException("User has invalid password. Password should be between 12-255 characters long, should contain 1 uppercase, 1 lowercase, 1 number and 1 special character(!.*_-)."))
                .when(validationService)
                .validateAppUserCreateDto(appUserCreateDto);

        final Exception exception = assertThrows(InvalidPasswordException.class, () -> userService.createUser(appUserCreateDto));

        assertEquals("User has invalid password. Password should be between 12-255 characters long, should contain 1 uppercase, 1 lowercase, 1 number and 1 special character(!.*_-).", exception.getMessage());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    public void givenValidUser_whenCreateUser_thenRawPasswordIsNotPersisted() {
        mockValidUserSetup();

        userService.createUser(appUserCreateDto);

        final ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());
        final AppUser savedAppUser = userCaptor.getValue();

        assertNotEquals("Password123!", savedAppUser.getPassword());
        assertEquals("hashed_Password123!", savedAppUser.getPassword());
    }

    @Test
    public void givenValidUser_whenFindById_thenUserIsReturned() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(appUser));

        final AppUserDto appUserDto = userService.getUserById(1L);

        assertNotNull(appUserDto);
        assertAll(
                () -> assertEquals("testuser", appUserDto.getUsername()),
                () -> assertEquals("test@example.com", appUserDto.getEmail()),
                () -> assertEquals("ADMIN", appUserDto.getRole())
        );

        verify(appUserRepository, times(1)).findById(1L);
    }

    @Test
    public void givenInvalidUser_whenFindById_thenThrowException() {
        final InvalidAppUserNameException exception = assertThrows(InvalidAppUserNameException.class,
                () -> userService.getUserById(99L));

        assertTrue(exception.getMessage().contains("Username does not exist"));
        verify(appUserRepository, times(1)).findById(99L);
    }

    @ParameterizedTest
    @MethodSource("providedFindByIdValidations")
    public void givenInvalidParameterValues_whenFindById_thenThrowException(final Long idValue,
                                                                            final String expectedMessage) {
        doThrow(new InvalidAppUserIDException("ID must be a positive number"))
                .when(validationService).validateUserId(idValue);
        final InvalidAppUserIDException exception = assertThrows(InvalidAppUserIDException.class,
                () -> userService.getUserById(idValue));

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void whenGetAllUsersIsCalled_thenReturnAllUsers() {
        final Pageable pageable = PageRequest.of(0, 2);
        final List<AppUser> appUsers = List.of(
                createTestUser(1L, "adminuser", "Ab1!" + "A".repeat(12), "adminuser@example.com", Role.ADMIN),
                createTestUser(2L, "testuser", "Ab1!" + "B".repeat(12), "testuser@example.com", Role.USER)
        );
        final Page<AppUser> userPage = new PageImpl<>(appUsers, pageable, 5);
        when(appUserRepository.findAll(pageable)).thenReturn(userPage);

        final Page<AppUserDto> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals("adminuser", result.getContent().getFirst().getUsername());
        assertEquals("testuser", result.getContent().get(1).getUsername());
    }

    @Test
    public void givenUsersExistWithRole_whenFindByRole_thenReturnsUserList() {
        final Role adminRole = Role.ADMIN;
        final Role userRole = Role.USER;
        final AppUser adminAppUser = createTestUser(1L, "adminuser", "Ab1!" + "A".repeat(12), "adminuser@example.com", Role.ADMIN);
        final AppUser testAppUser = createTestUser(2L, "testuser", "Ab1!" + "B".repeat(12), "testuser@example.com", Role.USER);
        final AppUser testAppUser1 = createTestUser(3L, "testuser1", "Ab1!" + "C".repeat(12), "testuser1@example.com", Role.USER);

        when(appUserRepository.findByRole(adminRole)).thenReturn(List.of(adminAppUser));
        when(appUserRepository.findByRole(userRole)).thenReturn(List.of(testAppUser, testAppUser1));

        final List<AppUserDto> adminDtoList = userService.getUsersByRole(adminRole);
        final List<AppUserDto> appUserDtoList = userService.getUsersByRole(userRole);
        assertNotNull(appUserDtoList);
        assertNotNull(adminDtoList);
        assertFalse(adminDtoList.isEmpty());
        assertFalse(appUserDtoList.isEmpty());
        assertEquals(1, adminDtoList.size());
        assertEquals(2, appUserDtoList.size());
        assertAll(
                () -> assertEquals("adminuser", adminDtoList.getFirst().getUsername()),
                () -> assertEquals("ADMIN", adminDtoList.getFirst().getRole()),
                () -> assertEquals("testuser", appUserDtoList.getFirst().getUsername()),
                () -> assertEquals("USER", appUserDtoList.getFirst().getRole()),
                () -> assertEquals("testuser1", appUserDtoList.get(1).getUsername()),
                () -> assertEquals("USER", appUserDtoList.get(1).getRole())
        );
    }

    @Test
    public void givenNoUsersExistWithRole_whenFindByRole_thenReturnsEmptyList() {
        final Role role = Role.ADMIN;
        when(appUserRepository.findByRole(role)).thenReturn(Collections.emptyList());

        final List<AppUserDto> result = userService.getUsersByRole(role);

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
    public void givenNoUsersExist_whenGetAll_thenReturnsEmptyPage() {
        when(appUserRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        final Pageable pageable = PageRequest.of(0, 20);
        final Page<AppUserDto> resultPage = userService.getAllUsers(pageable);

        assertNotNull(resultPage);
        assertTrue(resultPage.isEmpty());
    }

    @Test
    public void givenLargeNumberOfUsers_whenGetAll_thenReturnsFirstPageWith20Users() {
        final List<AppUser> largeAppUserList = IntStream
                .rangeClosed(1, 100)
                .mapToObj(i -> new AppUser((long) i, "user" + i, "email" + i + "@example.com", "Password123!", Role.USER))
                .toList();

        final Page<AppUser> pagedAppUsers = new PageImpl<>(largeAppUserList.subList(0, 20));

        when(appUserRepository.findAll(any(Pageable.class))).thenReturn(pagedAppUsers);

        Pageable pageable = PageRequest.of(0, 20);
        Page<AppUserDto> resultPage = userService.getAllUsers(pageable);

        assertNotNull(resultPage);
        assertEquals(20, resultPage.getNumberOfElements());
        assertEquals("user1", resultPage.getContent().getFirst().getUsername());
        assertEquals("user20", resultPage.getContent().get(19).getUsername());
    }

    @Test
    public void givenUsersExist_whenGetAll_thenMapsFieldsCorrectly() {
        final AppUser adminAppUser = createTestUser(1L, "adminuser", "Ab1!" + "A".repeat(12), "adminuser@example.com", Role.ADMIN);
        final Page<AppUser> pagedAppUsers = new PageImpl<>(List.of(adminAppUser));

        when(appUserRepository.findAll(any(Pageable.class))).thenReturn(pagedAppUsers);

        final Pageable pageable = PageRequest.of(0, 10);
        final Page<AppUserDto> resultPage = userService.getAllUsers(pageable);

        assertEquals(1, resultPage.getTotalElements());
        final AppUserDto appUserDto = resultPage.getContent().getFirst();
        assertEquals("adminuser", appUserDto.getUsername());
        assertEquals("adminuser@example.com", appUserDto.getEmail());
        assertEquals("ADMIN", appUserDto.getRole());
    }

    @Test
    public void givenLargeDataset_whenFindByRole_thenReturnsAllUsers() {
        final Role role = Role.USER;
        final List<AppUser> appUsers = IntStream
                .rangeClosed(1, 1000)
                .mapToObj(i -> new AppUser((long) i, "user" + i, "email" + i + "@example.com", "Password123!", role))
                .toList();

        when(appUserRepository.findByRole(role)).thenReturn(appUsers);

        final List<AppUserDto> result = userService.getUsersByRole(role);

        assertNotNull(result);
        assertEquals(1000, result.size());
    }

    @Test
    public void whenFindByRole_thenVerifyRepositoryInteraction() {
        final Role role = Role.USER;
        when(appUserRepository.findByRole(role)).thenReturn(Collections.emptyList());

        userService.getUsersByRole(role);

        verify(appUserRepository, times(1)).findByRole(role);
    }

    @Test
    public void givenMatchingSubstring_whenGetUsersByUsernameContains_thenReturnsUserList() {
        final String substring = "test";
        final Pageable pageable = PageRequest.of(0, 2);
        final List<AppUser> appUsers = List.of(
                createTestUser(1L, "testuser1", "Password123!", "email1@example.com", Role.USER),
                createTestUser(2L, "anothertestuser", "Password123!", "email2@example.com", Role.USER)
        );
        final Page<AppUser> userPage = new PageImpl<>(appUsers, pageable, 5);
        when(appUserRepository.findByUsernameContaining(substring, pageable)).thenReturn(userPage);

        final Page<AppUserDto> result = userService.getUsersByUsernameContains(substring, pageable);

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
        final Page<AppUser> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(appUserRepository.findByUsernameContaining(substring, pageable)).thenReturn(emptyPage);

        final Page<AppUserDto> result = userService.getUsersByUsernameContains(substring, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }

    @Test
    public void givenCaseInsensitiveMatch_whenGetUsersByUsernameContains_thenReturnsUsers() {
        final String substring = "Test";
        final Pageable pageable = PageRequest.of(0, 2);
        final List<AppUser> appUsers = List.of(
                createTestUser(1L, "testuser1", "Password123!", "email1@example.com", Role.USER),
                createTestUser(2L, "TestUser2", "Password123!", "email1@example.com", Role.USER)
        );
        final Page<AppUser> userPage = new PageImpl<>(appUsers, pageable, 5);
        when(appUserRepository.findByUsernameContaining(substring, pageable)).thenReturn(userPage);

        final Page<AppUserDto> result = userService.getUsersByUsernameContains(substring, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals("testuser1", result.getContent().getFirst().getUsername());
        assertEquals("TestUser2", result.getContent().get(1).getUsername());
    }

    @Test
    public void givenEmptySubstring_whenFindByUsernameContainsWithPagination_thenReturnsAllUsersPaginated() {
        final String substring = "";
        final Pageable pageable = PageRequest.of(0, 2); // Page 0, 2 results per page
        final List<AppUser> appUsers = List.of(
                createTestUser(1L, "user1", "Password123!", "email1@example.com", Role.USER),
                createTestUser(2L, "user2", "Password123!", "email2@example.com", Role.USER)
        );
        final Page<AppUser> userPage = new PageImpl<>(appUsers, pageable, 5);

        when(appUserRepository.findByUsernameContaining(substring, pageable)).thenReturn(userPage);

        final Page<AppUserDto> result = userService.getUsersByUsernameContains(substring, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals("user1", result.getContent().getFirst().getUsername());
        assertEquals("user2", result.getContent().get(1).getUsername());
    }

    @Test
    public void givenNullId_whenUpdateDto_thenThrowException() {
        doThrow(new InvalidAppUserIDException("ID must be a positive number"))
                .when(validationService).validateUserId(null);
        final AppUserUpdateDto appUserUpdateDto = new AppUserUpdateDto("updatedUsername", "updated@example.com");
        final Exception exception = assertThrows(InvalidAppUserIDException.class, () -> userService.updateUser(null, appUserUpdateDto));

        assertEquals("ID must be a positive number", exception.getMessage());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    public void givenNullUserUpdateDto_whenUpdateDto_thenThrowException() {
        doThrow(new NullParameterException("Parameter 'userUpdateDto' cannot be null"))
                .when(validationService).validateAppUserUpdateDto(any());
        final Exception exception = assertThrows(NullParameterException.class, () -> userService.updateUser(1L, null));

        assertEquals("Parameter 'userUpdateDto' cannot be null", exception.getMessage());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    public void givenValidIdAndUserUpdateDto_whenUpdateUser_thenReturnUpdatedUserDto() {
        final Long id = 1L;
        final AppUserUpdateDto appUserUpdateDto = new AppUserUpdateDto("updatedUsername", "updated@example.com");
        final AppUser existingAppUser = createTestUser(id, "oldUsername", "Password123!", "old@example.com", Role.USER);

        when(appUserRepository.findById(id)).thenReturn(Optional.of(existingAppUser));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(validationService).validateAppUserUpdateDto(appUserUpdateDto);

        final AppUserDto result = userService.updateUser(id, appUserUpdateDto);

        assertNotNull(result);
        assertEquals("updatedUsername", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        verify(validationService, times(1)).validateAppUserUpdateDto(appUserUpdateDto);
    }

    @Test
    public void givenNonExistingId_whenUpdateUser_thenThrowUserNotFoundException() {
        final Long id = 1L;
        final AppUserUpdateDto appUserUpdateDto = new AppUserUpdateDto("updatedUsername", "updated@example.com");
        when(appUserRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(AppUserNotFoundException.class, () -> userService.updateUser(id, appUserUpdateDto));
        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(validationService, times(1)).validateAppUserUpdateDto(appUserUpdateDto);
    }

    @Test
    public void givenInvalidUserUpdateDto_whenUpdateUser_thenThrowValidationException() {
        final Long id = 1L;
        final AppUserUpdateDto appUserUpdateDto = new AppUserUpdateDto(null, "invalid-email");
        doThrow(new InvalidAppUserNameException("Username can not be null."))
                .when(validationService).validateAppUserUpdateDto(any());
        assertThrows(InvalidAppUserNameException.class, () -> userService.updateUser(id, appUserUpdateDto));
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    public void givenPartialUserUpdateDto_whenUpdateUser_thenOnlyUpdateNonNullFields() {
        final Long id = 1L;
        final AppUserUpdateDto appUserUpdateDto = new AppUserUpdateDto(null, "updated@example.com");
        final AppUser existingAppUser = createTestUser(id, "oldUsername", "Password123!", "old@example.com", Role.USER);

        when(appUserRepository.findById(id)).thenReturn(Optional.of(existingAppUser));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(validationService).validateAppUserUpdateDto(appUserUpdateDto);

        final AppUserDto result = userService.updateUser(id, appUserUpdateDto);

        assertNotNull(result);
        assertEquals("oldUsername", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        verify(validationService, times(1)).validateAppUserUpdateDto(appUserUpdateDto);
    }

    @Test
    public void whenUpdateUser_thenVerifyRepositoryInteractions() {
        final Long id = 1L;
        final AppUserUpdateDto appUserUpdateDto = new AppUserUpdateDto("updatedUsername", "updated@example.com");
        final AppUser existingAppUser = createTestUser(id, "oldUsername", "Password123!", "old@example.com", Role.USER);

        when(appUserRepository.findById(id)).thenReturn(Optional.of(existingAppUser));
        doNothing().when(validationService).validateAppUserUpdateDto(appUserUpdateDto);
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(id, appUserUpdateDto);

        verify(appUserRepository, times(1)).findById(id);
        verify(appUserRepository, times(1)).save(any(AppUser.class));
        verify(validationService, times(1)).validateAppUserUpdateDto(appUserUpdateDto);
    }

    @Test
    public void givenEmailAlreadyExists_whenUpdateUser_thenThrowDuplicateEmailException() {
        final Long id = 1L;
        final AppUserUpdateDto appUserUpdateDto = new AppUserUpdateDto("updatedUsername", "existing@example.com");

        when(appUserRepository.existsByEmail("existing@example.com")).thenReturn(true);
        doNothing().when(validationService).validateAppUserUpdateDto(appUserUpdateDto);

        assertThrows(DuplicateEmailException.class, () -> userService.updateUser(id, appUserUpdateDto));
        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(validationService, times(1)).validateAppUserUpdateDto(appUserUpdateDto);
    }

    @Test
    public void givenValidId_whenDeleteUser_thenUserIsDeleted() {
        final Long id = 1L;
        doNothing().when(appUserRepository).deleteById(id);

        userService.deleteUser(id);

        verify(appUserRepository, times(1)).deleteById(id);
    }

    @Test
    public void givenNullId_whenDeleteUser_thenThrowNullParameterException() {
        assertThrows(InvalidAppUserIDException.class, () -> userService.deleteUser(null));
        verify(appUserRepository, never()).deleteById(any());
    }

    @Test
    public void givenNonExistentId_whenDeleteUser_thenDoNothing() {
        final Long id = 99L;
        doNothing().when(appUserRepository).deleteById(id);

        userService.deleteUser(id);

        verify(appUserRepository, times(1)).deleteById(id);
    }

    @Test
    public void givenExistingUsername_whenUsernameExists_thenReturnTrue() {
        final String existingUsername = "testuser";
        when(appUserRepository.existsByUsername(existingUsername)).thenReturn(true);

        final boolean result = userService.usernameExists(existingUsername);

        assertTrue(result);
        verify(appUserRepository, times(1)).existsByUsername(existingUsername);
    }

    @Test
    public void givenNonExistingUsername_whenUsernameExists_thenReturnFalse() {
        final String nonExistingUsername = "nonexistentuser";
        when(appUserRepository.existsByUsername(nonExistingUsername)).thenReturn(false);

        final boolean result = userService.usernameExists(nonExistingUsername);

        assertFalse(result);
        verify(appUserRepository, times(1)).existsByUsername(nonExistingUsername);
    }

    @Test
    public void givenNullUsername_whenUsernameExists_thenThrowNullParameterException() {
        assertThrows(NullParameterException.class, () -> userService.usernameExists(null));
        verify(appUserRepository, never()).existsByUsername(any());
    }

    @Test
    public void givenBlankUsername_whenUsernameExists_thenThrowBlankParameterException() {
        assertThrows(BlankParameterException.class, () -> userService.usernameExists("   "));
        verify(appUserRepository, never()).existsByUsername(any());
    }

    @Test
    public void givenUsernameInDifferentCase_whenUsernameExists_thenReturnCorrectResult() {
        final String username = "TestUser";
        when(appUserRepository.existsByUsername("TestUser")).thenReturn(true);

        final boolean result = userService.usernameExists(username);

        assertTrue(result);
        verify(appUserRepository, times(1)).existsByUsername(username);
    }

    @Test
    public void givenExistingEmail_whenEmailExists_thenReturnTrue() {
        final String existingEmail = "test@example.com";
        when(appUserRepository.existsByEmail(existingEmail)).thenReturn(true);

        final boolean result = userService.emailExists(existingEmail);

        assertTrue(result);
        verify(appUserRepository, times(1)).existsByEmail(existingEmail);
    }

    @Test
    public void givenNonExistingEmail_whenEmailExists_thenReturnFalse() {
        final String nonExistingEmail = "nonexistent@example.com";
        when(appUserRepository.existsByEmail(nonExistingEmail)).thenReturn(false);

        final boolean result = userService.emailExists(nonExistingEmail);

        assertFalse(result);
        verify(appUserRepository, times(1)).existsByEmail(nonExistingEmail);
    }

    @Test
    public void givenNullEmail_whenEmailExists_thenThrowNullParameterException() {
        assertThrows(NullParameterException.class, () -> userService.emailExists(null));
        verify(appUserRepository, never()).existsByEmail(any());
    }

    @Test
    public void givenBlankEmail_whenEmailExists_thenThrowBlankParameterException() {
        assertThrows(BlankParameterException.class, () -> userService.emailExists("   "));
        verify(appUserRepository, never()).existsByEmail(any());
    }

    @Test
    public void givenEmailInDifferentCase_whenEmailExists_thenReturnCorrectResult() {
        final String email = "Test@Example.com";
        when(appUserRepository.existsByEmail("Test@Example.com")).thenReturn(true);

        final boolean result = userService.emailExists(email);

        assertTrue(result);
        verify(appUserRepository, times(1)).existsByEmail(email);
    }

    @Test
    public void givenValidUserIdAndRole_whenHasRole_thenReturnTrue() {
        final Long userId = 1L;
        final Role role = Role.ADMIN;
        when(appUserRepository.findByIdAndRole(userId, role)).thenReturn(true);

        final boolean result = userService.hasRole(userId, role);

        assertTrue(result);
        verify(appUserRepository, times(1)).findByIdAndRole(userId, role);
    }

    @Test
    public void givenUserIdAndRoleNotMatching_whenHasRole_thenReturnFalse() {
        final Long userId = 1L;
        final Role role = Role.USER;
        when(appUserRepository.findByIdAndRole(userId, role)).thenReturn(false);

        final boolean result = userService.hasRole(userId, role);

        assertFalse(result);
        verify(appUserRepository, times(1)).findByIdAndRole(userId, role);
    }

    @Test
    public void givenNullUserId_whenHasRole_thenThrowNullParameterException() {
        final Role role = Role.ADMIN;

        assertThrows(InvalidAppUserIDException.class, () -> userService.hasRole(null, role));
        verify(appUserRepository, never()).findByIdAndRole(any(), any());
    }

    @Test
    public void givenNegativeUserId_whenHasRole_thenThrowInvalidUserIDException() {
        final Long userId = -1L;
        final Role role = Role.ADMIN;

        assertThrows(InvalidAppUserIDException.class, () -> userService.hasRole(userId, role));
        verify(appUserRepository, never()).findByIdAndRole(any(), any());
    }

    @Test
    public void givenZeroUserId_whenHasRole_thenThrowInvalidUserIDException() {
        final Long userId = 0L;
        final Role role = Role.ADMIN;

        assertThrows(InvalidAppUserIDException.class, () -> userService.hasRole(userId, role));
        verify(appUserRepository, never()).findByIdAndRole(any(), any());
    }

    @Test
    public void givenNullRole_whenHasRole_thenThrowNullParameterException() {
        final Long userId = 1L;

        assertThrows(NullParameterException.class, () -> userService.hasRole(userId, null));
        verify(appUserRepository, never()).findByIdAndRole(any(), any());
    }

    private static Stream<Arguments> providedFindByIdValidations() {
        return Stream.of(
                Arguments.of(null, "ID must be a positive number"),  // Null value
                Arguments.of(-1L, "ID must be a positive number"),   // Negative value
                Arguments.of(0L, "ID must be a positive number")     // Boundary value
        );
    }

    private static AppUser createTestUser(final Long id, final String username, final String password, final String email, final Role role) {
        return AppUser.builder().id(id).username(username).password(password).email(email).role(role).build();
    }

    private void mockValidUserSetup() {
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(hashingService.hashPassword(appUserCreateDto.getPassword())).thenReturn("hashed_Password123!");
    }
}