package be.jensberckmoes.personal_finance_tracker.integration.repository;

import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.model.User;
import be.jensberckmoes.personal_finance_tracker.repository.UserRepository;
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
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private HashingService hashingService;
    @MockitoBean
    private SecurityFilterChain mockSecurityFilterChain;

    private User user;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .password(hashingService.hashPassword("Password123!")) // Use hashed password
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .build();
    }

    @Test
    public void givenUser_whenSave_thenPersistUser() {
        final User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertNotEquals("Password123!", savedUser.getPassword());
        assertEquals("test@example.com", savedUser.getEmail());
    }

    @Test
    public void givenNullUsername_whenSave_thenThrowException() {
        user.setUsername(null);

        assertThrows(Exception.class, () -> userRepository.save(user));
    }

    @ParameterizedTest
    @MethodSource("providedUsernameInputs")
    public void givenInvalidUsernames_whenSave_thenThrowException(final String invalidUsername,
                                                                  final String expectedMessage) {
        user.setUsername(invalidUsername);

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> userRepository.save(user));

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void givenNullPassword_whenSave_thenThrowException() {
        user.setPassword(null);

        assertThrows(Exception.class, () -> userRepository.save(user));
    }

    @ParameterizedTest
    @MethodSource("providedPasswordInputs")
    public void givenInvalidPasswords_whenSave_thenThrowException(final String invalidPassword,
                                                                  final String expectedMessage) {
        user.setPassword(invalidPassword);

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> userRepository.save(user));

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void givenNullEmail_whenSave_thenThrowException() {
        user.setEmail(null);

        assertThrows(Exception.class, () -> userRepository.save(user));
    }

    @ParameterizedTest
    @MethodSource("providedEmailInputs")
    public void givenInvalidEmails_whenSave_thenThrowException(final String invalidUsername,
                                                               final String expectedMessage) {
        user.setEmail(invalidUsername);

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> userRepository.save(user));

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void givenNullRole_whenSave_thenThrowException() {
        user.setRole(null);

        assertThrows(Exception.class, () -> userRepository.save(user));
    }

    @Test
    public void givenUserWithoutEnumTypeString_whenSave_thenRoleIsStoredAsOrdinal() {
        user.setRole(Role.ADMIN);
        final User savedUser = userRepository.save(user);

        final User foundUser = entityManager.find(User.class, user.getId());
        assertNotNull(foundUser);
        assertEquals(Role.ADMIN, savedUser.getRole()); // Role object check

        final var roleValue = entityManager
                .createNativeQuery("SELECT role FROM app_user WHERE id = :id")
                .setParameter("id", user.getId())
                .getSingleResult();

        assertEquals("ADMIN", roleValue);
    }

    @Test
    public void givenUserWithUserRole_whenSave_thenPersistRoleAsUser() {
        final User savedUser = userRepository.save(user);

        assertEquals(Role.USER, savedUser.getRole());
    }

    @Test
    public void givenUserWithAdminRole_whenSave_thenPersistRole() {
        user.setRole(Role.ADMIN);

        final User savedUser = userRepository.save(user);

        assertEquals(Role.ADMIN, savedUser.getRole());
    }

    @Test
    public void givenExistingUsername_whenFindByUsername_thenReturnUser() {
        userRepository.save(user);

        final Optional<User> possibleFoundUser = userRepository.findByUsername(user.getUsername());

        assertTrue(possibleFoundUser.isPresent());
        assertEquals(user.getUsername(), possibleFoundUser.get().getUsername());
    }

    @Test
    public void givenNonExistingUsername_whenFindByUsername_thenReturnEmptyOptional() {
        final Optional<User> possibleFoundUser = userRepository.findByUsername("nonexistent");

        assertFalse(possibleFoundUser.isPresent());
    }

    @Test
    public void givenDuplicateUsername_whenSave_thenThrowException() {
        userRepository.save(user);

        final User duplicateUser = User.builder()
                .password("Password123!")
                .username("testuser")
                .email("test2@example.com")
                .role(Role.USER)
                .build();

        assertThrows(Exception.class, () -> userRepository.save(duplicateUser));
    }

    @Test
    public void givenDuplicateEmail_whenSave_thenThrowException() {
        userRepository.save(user);

        final User duplicateUser = User.builder()
                .password("Password123!")
                .username("testuser2")
                .email("test@example.com")
                .role(Role.USER)
                .build();

        assertThrows(Exception.class, () -> userRepository.save(duplicateUser));
    }

    @Test
    public void givenUserWithHashedPassword_whenSave_thenPasswordIsPersisted() {
        final String hashedPassword = hashingService.hashPassword("Password123!");
        user.setPassword(hashedPassword);
        userRepository.saveAndFlush(user);

        final Optional<User> possibleFoundUser = userRepository.findByUsername(user.getUsername());

        // Assert
        assertNotNull(possibleFoundUser);
        assertTrue(possibleFoundUser.isPresent());
        final String foundUserPassword = possibleFoundUser.get().getPassword();
        assertEquals(hashedPassword, foundUserPassword);
        assertNotEquals("Password123!", foundUserPassword);
    }

    @Test
    public void givenRole_whenFindAllByRole_thenReturnUsersWithThatRole() {
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        final User user2 = User.builder()
                .password(hashingService.hashPassword("Password123!")) // Use hashed password
                .username("testuser2")
                .email("test2@example.com")
                .role(Role.USER)
                .build();
        userRepository.save(user2);

        final User user3 = User.builder()
                .password(hashingService.hashPassword("Password123!")) // Use hashed password
                .username("testuser3")
                .email("test3@example.com")
                .role(Role.ADMIN)
                .build();
        userRepository.save(user3);

        final List<User> admins = userRepository.findByRole(Role.ADMIN);
        final List<User> users = userRepository.findByRole(Role.USER);
        assertNotNull(admins);
        assertEquals(2, admins.size());
        assertEquals(1, users.size());
        assertEquals(Role.ADMIN, admins.getFirst().getRole());
        assertEquals(Role.ADMIN, admins.get(1).getRole());
        assertEquals(Role.USER, users.getFirst().getRole());
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