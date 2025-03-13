package be.jensberckmoes.personal_finance_tracker.integration.repository;

import be.jensberckmoes.personal_finance_tracker.model.AppUser;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.repository.AppUserRepository;
import be.jensberckmoes.personal_finance_tracker.service.HashingService;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class AppAppUserRepositoryTest {
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private HashingService hashingService;
    @MockitoBean
    private SecurityFilterChain mockSecurityFilterChain;

    private AppUser appUser;

    @BeforeEach
    public void setUp() {
        appUser = AppUser.builder()
                .password(hashingService.hashPassword("Password123!")) // Use hashed password
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .build();
    }

    @Test
    public void givenUser_whenSave_thenPersistUser() {
        final AppUser savedAppUser = appUserRepository.save(appUser);

        assertNotNull(savedAppUser.getId());
        assertEquals("testuser", savedAppUser.getUsername());
        assertNotEquals("Password123!", savedAppUser.getPassword());
        assertEquals("test@example.com", savedAppUser.getEmail());
    }

    @Test
    public void givenNullUsername_whenSave_thenThrowException() {
        appUser.setUsername(null);

        assertThrows(Exception.class, () -> appUserRepository.save(appUser));
    }

    @ParameterizedTest
    @MethodSource("providedUsernameInputs")
    public void givenInvalidUsernames_whenSave_thenThrowException(final String invalidUsername,
                                                                  final String expectedMessage) {
        appUser.setUsername(invalidUsername);

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> appUserRepository.save(appUser));

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void givenNullPassword_whenSave_thenThrowException() {
        appUser.setPassword(null);

        assertThrows(Exception.class, () -> appUserRepository.save(appUser));
    }

    @ParameterizedTest
    @MethodSource("providedPasswordInputs")
    public void givenInvalidPasswords_whenSave_thenThrowException(final String invalidPassword,
                                                                  final String expectedMessage) {
        appUser.setPassword(invalidPassword);

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> appUserRepository.save(appUser));

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void givenNullEmail_whenSave_thenThrowException() {
        appUser.setEmail(null);

        assertThrows(Exception.class, () -> appUserRepository.save(appUser));
    }

    @ParameterizedTest
    @MethodSource("providedEmailInputs")
    public void givenInvalidEmails_whenSave_thenThrowException(final String invalidUsername,
                                                               final String expectedMessage) {
        appUser.setEmail(invalidUsername);

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> appUserRepository.save(appUser));

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void givenNullRole_whenSave_thenThrowException() {
        appUser.setRole(null);

        assertThrows(Exception.class, () -> appUserRepository.save(appUser));
    }

    @Test
    public void givenUserWithoutEnumTypeString_whenSave_thenRoleIsStoredAsOrdinal() {
        appUser.setRole(Role.ADMIN);
        final AppUser savedAppUser = appUserRepository.save(appUser);

        final AppUser foundAppUser = entityManager.find(AppUser.class, appUser.getId());
        assertNotNull(foundAppUser);
        assertEquals(Role.ADMIN, savedAppUser.getRole()); // Role object check

        final var roleValue = entityManager
                .createNativeQuery("SELECT role FROM app_user WHERE id = :id")
                .setParameter("id", appUser.getId())
                .getSingleResult();

        assertEquals("ADMIN", roleValue);
    }

    @Test
    public void givenUserWithUserRole_whenSave_thenPersistRoleAsUser() {
        final AppUser savedAppUser = appUserRepository.save(appUser);

        assertEquals(Role.USER, savedAppUser.getRole());
    }

    @Test
    public void givenUserWithAdminRole_whenSave_thenPersistRole() {
        appUser.setRole(Role.ADMIN);

        final AppUser savedAppUser = appUserRepository.save(appUser);

        assertEquals(Role.ADMIN, savedAppUser.getRole());
    }

    @Test
    public void givenExistingUsername_whenFindByUsername_thenReturnUser() {
        appUserRepository.save(appUser);

        final Optional<AppUser> possibleFoundUser = appUserRepository.findByUsername(appUser.getUsername());

        assertTrue(possibleFoundUser.isPresent());
        assertEquals(appUser.getUsername(), possibleFoundUser.get().getUsername());
    }

    @Test
    public void givenNonExistingUsername_whenFindByUsername_thenReturnEmptyOptional() {
        final Optional<AppUser> possibleFoundUser = appUserRepository.findByUsername("nonexistent");

        assertFalse(possibleFoundUser.isPresent());
    }

    @Test
    public void givenDuplicateUsername_whenSave_thenThrowException() {
        appUserRepository.save(appUser);

        final AppUser duplicateAppUser = AppUser.builder()
                .password("Password123!")
                .username("testuser")
                .email("test2@example.com")
                .role(Role.USER)
                .build();

        assertThrows(Exception.class, () -> appUserRepository.save(duplicateAppUser));
    }

    @Test
    public void givenDuplicateEmail_whenSave_thenThrowException() {
        appUserRepository.save(appUser);

        final AppUser duplicateAppUser = AppUser.builder()
                .password("Password123!")
                .username("testuser2")
                .email("test@example.com")
                .role(Role.USER)
                .build();

        assertThrows(Exception.class, () -> appUserRepository.save(duplicateAppUser));
    }

    @Test
    public void givenUserWithHashedPassword_whenSave_thenPasswordIsPersisted() {
        final String hashedPassword = hashingService.hashPassword("Password123!");
        appUser.setPassword(hashedPassword);
        appUserRepository.saveAndFlush(appUser);

        final Optional<AppUser> possibleFoundUser = appUserRepository.findByUsername(appUser.getUsername());

        // Assert
        assertNotNull(possibleFoundUser);
        assertTrue(possibleFoundUser.isPresent());
        final String foundUserPassword = possibleFoundUser.get().getPassword();
        assertEquals(hashedPassword, foundUserPassword);
        assertNotEquals("Password123!", foundUserPassword);
    }

    @Test
    public void givenRole_whenFindAllByRole_thenReturnUsersWithThatRole() {
        appUser.setRole(Role.ADMIN);
        appUserRepository.save(appUser);

        final AppUser appUser2 = AppUser.builder()
                .password(hashingService.hashPassword("Password123!")) // Use hashed password
                .username("testuser2")
                .email("test2@example.com")
                .role(Role.USER)
                .build();
        appUserRepository.save(appUser2);

        final AppUser appUser3 = AppUser.builder()
                .password(hashingService.hashPassword("Password123!")) // Use hashed password
                .username("testuser3")
                .email("test3@example.com")
                .role(Role.ADMIN)
                .build();
        appUserRepository.save(appUser3);

        final List<AppUser> admins = appUserRepository.findByRole(Role.ADMIN);
        final List<AppUser> appUsers = appUserRepository.findByRole(Role.USER);
        assertNotNull(admins);
        assertEquals(2, admins.size());
        assertEquals(1, appUsers.size());
        assertEquals(Role.ADMIN, admins.getFirst().getRole());
        assertEquals(Role.ADMIN, admins.get(1).getRole());
        assertEquals(Role.USER, appUsers.getFirst().getRole());
    }

    private static Stream<Arguments> providedUsernameInputs() {
        return Stream.of(
                Arguments.of("", "Username must be between 3 and 20 characters."),                                  // Empty string
                Arguments.of("AB", "Username must be between 3 and 20 characters."),                                        // 2 length (<3)
                Arguments.of("A".repeat(21), "Username must be between 3 and 20 characters."),                        // 21 length (>20)
                Arguments.of("Ab!".repeat(4), "Username can only contain letters, numbers, dots, or underscores.")    // Invalid regex
        );
    }

    private static Stream<Arguments> providedPasswordInputs() {
        return Stream.of(
                Arguments.of("", "Password must be between 12 and 255 characters."),                            // Empty string
                Arguments.of("Ab1!" + "A".repeat(7), "Password must be between 12 and 255 characters."),  // 11 length (<12)
                Arguments.of("Ab1!" + "A".repeat(252), "Password must be between 12 and 255 characters.") // 256 length (>255)
        );
    }

    private static Stream<Arguments> providedEmailInputs() {
        return Stream.of(
                Arguments.of("", "E-mail must be between 2 and 255 characters."),                                                                                 // Empty string
                Arguments.of("A", "E-mail must be between 2 and 255 characters."),                                                                                // 1 length (<2)
                Arguments.of("a".repeat(126) + "@" + "b".repeat(126) + ".be", "E-mail must be between 2 and 255 characters."),                        // 256 length (>255)
                Arguments.of("awesome!dude@gmail.com", "E-mail must be in following format: (something@test.com)")                                                // Invalid regex
        );
    }
}