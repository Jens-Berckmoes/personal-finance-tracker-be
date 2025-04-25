package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.dto.AppUserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserUpdateDto;
import be.jensberckmoes.personal_finance_tracker.exception.*;
import be.jensberckmoes.personal_finance_tracker.service.AppUserValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AppUserValidationServiceTest {

    @Autowired
    private AppUserValidationService validationService;

    @Test
    void givenNullAppUserCreateDto_whenValidateAppUserCreateDto_thenThrowsNullParameterException() {
        assertThrows(NullParameterException.class, () -> validationService.validateAppUserCreateDto(null));
    }

    @Test
    void givenValidAppUserCreateDto_whenValidateAppUserCreateDto_thenDoesNotThrowException() {
        final AppUserCreateDto validDto = AppUserCreateDto.builder()
                .username("testuser")
                .password("StrongPass1!")
                .email("test@example.com")
                .build();
        assertDoesNotThrow(() -> validationService.validateAppUserCreateDto(validDto));
    }

    @ParameterizedTest
    @MethodSource("providedInvalidLengthUsernameInputs")
    void givenInvalidAppUserCreateDtoWithInvalidLengthUsername_whenValidateAppUserCreateDto_thenThrowsInvalidAppUserNameException(String username) {
        final AppUserCreateDto invalidDto = AppUserCreateDto.builder()
                .username(username)
                .password("StrongPass1!")
                .email("test@example.com")
                .build();
        assertThrows(InvalidAppUserNameException.class, () -> validationService.validateAppUserCreateDto(invalidDto));
    }

    @ParameterizedTest
    @MethodSource("providedInvalidUsernameCharacterInputs")
    void givenInvalidAppUserCreateDtoWithInvalidUsernameCharacters_whenValidateAppUserCreateDto_thenThrowsInvalidAppUserNameException(String username) {
        final AppUserCreateDto invalidDto = AppUserCreateDto.builder()
                .username(username)
                .password("StrongPass1!")
                .email("test@example.com")
                .build();
        assertThrows(InvalidAppUserNameException.class, () -> validationService.validateAppUserCreateDto(invalidDto));
    }

    @Test
    void givenInvalidAppUserCreateDtoWithNullUsername_whenValidateAppUserCreateDto_thenThrowsInvalidAppUserNameException() {
        final AppUserCreateDto invalidDto = AppUserCreateDto.builder()
                .username(null)
                .password("StrongPass1!")
                .email("test@example.com")
                .build();
        assertThrows(InvalidAppUserNameException.class, () -> validationService.validateAppUserCreateDto(invalidDto));
    }

    @Test
    void givenAppUserCreateDtoWithUsernameAtMinLength_whenValidateAppUserCreateDto_thenDoesNotThrowException() {
        final AppUserCreateDto validDto = AppUserCreateDto.builder()
                .username("abc")
                .password("StrongPass1!")
                .email("test@example.com")
                .build();
        assertDoesNotThrow(() -> validationService.validateAppUserCreateDto(validDto));
    }

    @Test
    void givenAppUserCreateDtoWithUsernameAtMaxLength_whenValidateAppUserCreateDto_thenDoesNotThrowException() {
        final AppUserCreateDto validDto = AppUserCreateDto.builder()
                .username("abcdefghijklmnopqrst")
                .password("StrongPass1!")
                .email("test@example.com")
                .build();
        assertDoesNotThrow(() -> validationService.validateAppUserCreateDto(validDto));
    }

    @ParameterizedTest
    @MethodSource("providedMissingPasswordCriteriaInputs")
    void givenInvalidAppUserCreateDtoWithMissingPasswordCriteria_whenValidateAppUserCreateDto_thenThrowsInvalidPasswordException(String password) {
        final AppUserCreateDto invalidDto = AppUserCreateDto.builder()
                .username("testuser")
                .password(password)
                .email("test@example.com")
                .build();
        assertThrows(InvalidPasswordException.class, () -> validationService.validateAppUserCreateDto(invalidDto));
    }

    @Test
    void givenInvalidAppUserCreateDtoWithPasswordBelowMinLength_whenValidateAppUserCreateDto_thenThrowsInvalidPasswordException() {
        final AppUserCreateDto invalidDto = AppUserCreateDto.builder()
                .username("testuser")
                .password("Weak1!")
                .email("test@example.com")
                .build();
        assertThrows(InvalidPasswordException.class, () -> validationService.validateAppUserCreateDto(invalidDto));
    }

    @Test
    void givenInvalidAppUserCreateDtoWithPasswordAboveMaxLength_whenValidateAppUserCreateDto_thenThrowsInvalidPasswordException() {
        final AppUserCreateDto invalidDto = AppUserCreateDto.builder()
                .username("testuser")
                .password("A1!".repeat(70)) // Length > 255
                .email("test@example.com")
                .build();
        assertThrows(InvalidPasswordException.class, () -> validationService.validateAppUserCreateDto(invalidDto));
    }

    @Test
    void givenAppUserCreateDtoWithPasswordAtMinLength_whenValidateAppUserCreateDto_thenDoesNotThrowException() {
        final AppUserCreateDto validDto = AppUserCreateDto.builder()
                .username("testuser")
                .password("StrongPass1!")
                .email("test@example.com")
                .build();
        assertDoesNotThrow(() -> validationService.validateAppUserCreateDto(validDto));
    }

    @Test
    void givenAppUserCreateDtoWithPasswordAtMaxLength_whenValidateAppUserCreateDto_thenDoesNotThrowException() {
        final AppUserCreateDto validDto = AppUserCreateDto.builder()
                .username("testuser")
                .password("Aa1!".repeat(63)+"123") // Length 255
                .email("test@example.com")
                .build();
        assertDoesNotThrow(() -> validationService.validateAppUserCreateDto(validDto));
    }

    @Test
    void givenInvalidAppUserCreateDtoWithNullPassword_whenValidateAppUserCreateDto_thenThrowsInvalidPasswordException() {
        final AppUserCreateDto invalidDto = AppUserCreateDto.builder()
                .username("testuser")
                .password(null)
                .email("test@example.com")
                .build();
        assertThrows(InvalidPasswordException.class, () -> validationService.validateAppUserCreateDto(invalidDto));
    }

    @ParameterizedTest
    @MethodSource("providedInvalidEmailInputs")
    void givenInvalidAppUserCreateDtoWithInvalidEmail_whenValidateAppUserCreateDto_thenThrowsInvalidEmailException(String email) {
        final AppUserCreateDto invalidDto = AppUserCreateDto.builder()
                .username("testuser")
                .password("StrongPass1!")
                .email(email)
                .build();
        assertThrows(InvalidEmailException.class, () -> validationService.validateAppUserCreateDto(invalidDto));
    }

    @Test
    void givenInvalidAppUserCreateDtoWithNullEmail_whenValidateAppUserCreateDto_thenThrowsInvalidEmailException() {
        final AppUserCreateDto invalidDto = AppUserCreateDto.builder()
                .username("testuser")
                .password("StrongPass1!")
                .email(null)
                .build();
        assertThrows(InvalidEmailException.class, () -> validationService.validateAppUserCreateDto(invalidDto));
    }

    @Test
    void givenAppUserCreateDtoWithEmailAtMinLength_whenValidateAppUserCreateDto_thenDoesNotThrowException() {
        final AppUserCreateDto validDto = AppUserCreateDto.builder()
                .username("testuser")
                .password("StrongPass1!")
                .email("a@a.aa") // Length 4, but email content min is 2 chars before @ and 2 after
                .build();
        assertDoesNotThrow(() -> validationService.validateAppUserCreateDto(validDto));
    }

    @Test
    void givenAppUserCreateDtoWithEmailAtMaxLength_whenValidateAppUserCreateDto_thenDoesNotThrowException() {
        final AppUserCreateDto validDto = AppUserCreateDto.builder()
                .username("testuser")
                .password("StrongPass1!")
                .email("a".repeat(243) + "@example.com") // Length 255
                .build();
        assertDoesNotThrow(() -> validationService.validateAppUserCreateDto(validDto));
    }

    @Test
    void givenNullAppUserUpdateDto_whenValidateAppUserUpdateDto_thenThrowsNullParameterException() {
        assertThrows(NullParameterException.class, () -> validationService.validateAppUserUpdateDto(null));
    }

    @Test
    void givenValidAppUserUpdateDto_whenValidateAppUserUpdateDto_thenDoesNotThrowException() {
        final AppUserUpdateDto validDto = AppUserUpdateDto.builder()
                .username("updateduser")
                .email("updated@example.com")
                .build();
        assertDoesNotThrow(() -> validationService.validateAppUserUpdateDto(validDto));
    }

    @ParameterizedTest
    @MethodSource("providedInvalidLengthUsernameInputs")
    void givenInvalidAppUserUpdateDtoWithInvalidLengthUsername_whenValidateAppUserUpdateDto_thenThrowsInvalidAppUserNameException(String username) {
        final AppUserUpdateDto invalidDto = AppUserUpdateDto.builder()
                .username(username)
                .email("updated@example.com")
                .build();
        assertThrows(InvalidAppUserNameException.class, () -> validationService.validateAppUserUpdateDto(invalidDto));
    }

    @ParameterizedTest
    @MethodSource("providedInvalidUsernameCharacterInputs")
    void givenInvalidAppUserUpdateDtoWithInvalidUsernameCharacters_whenValidateAppUserUpdateDto_thenThrowsInvalidAppUserNameException(String username) {
        final AppUserUpdateDto invalidDto = AppUserUpdateDto.builder()
                .username(username)
                .email("updated@example.com")
                .build();
        assertThrows(InvalidAppUserNameException.class, () -> validationService.validateAppUserUpdateDto(invalidDto));
    }

    @Test
    void givenInvalidAppUserUpdateDtoWithNullUsername_whenValidateAppUserUpdateDto_thenThrowsInvalidAppUserNameException() {
        final AppUserUpdateDto invalidDto = AppUserUpdateDto.builder()
                .username(null)
                .email("updated@example.com")
                .build();
        assertThrows(InvalidAppUserNameException.class, () -> validationService.validateAppUserUpdateDto(invalidDto));
    }

    @Test
    void givenAppUserUpdateDtoWithUsernameAtMinLength_whenValidateAppUserUpdateDto_thenDoesNotThrowException() {
        final AppUserUpdateDto validDto = AppUserUpdateDto.builder()
                .username("abc")
                .email("updated@example.com")
                .build();
        assertDoesNotThrow(() -> validationService.validateAppUserUpdateDto(validDto));
    }

    @Test
    void givenAppUserUpdateDtoWithUsernameAtMaxLength_whenValidateAppUserUpdateDto_thenDoesNotThrowException() {
        final AppUserUpdateDto validDto = AppUserUpdateDto.builder()
                .username("abcdefghijklmnopqrst")
                .email("updated@example.com")
                .build();
        assertDoesNotThrow(() -> validationService.validateAppUserUpdateDto(validDto));
    }

    @ParameterizedTest
    @MethodSource("providedInvalidEmailInputs")
    void givenInvalidAppUserUpdateDtoWithInvalidEmail_whenValidateAppUserUpdateDto_thenThrowsInvalidEmailException(String email) {
        final AppUserUpdateDto invalidDto = AppUserUpdateDto.builder()
                .username("updateduser")
                .email(email)
                .build();
        assertThrows(InvalidEmailException.class, () -> validationService.validateAppUserUpdateDto(invalidDto));
    }

    @Test
    void givenInvalidAppUserUpdateDtoWithNullEmail_whenValidateAppUserUpdateDto_thenThrowsInvalidEmailException() {
        final AppUserUpdateDto invalidDto = AppUserUpdateDto.builder()
                .username("updateduser")
                .email(null)
                .build();
        assertThrows(InvalidEmailException.class, () -> validationService.validateAppUserUpdateDto(invalidDto));
    }

    @Test
    void givenAppUserUpdateDtoWithEmailAtMinLength_whenValidateAppUserUpdateDto_thenDoesNotThrowException() {
        final AppUserUpdateDto validDto = AppUserUpdateDto.builder()
                .username("updateduser")
                .email("a@a.aa")
                .build();
        assertDoesNotThrow(() -> validationService.validateAppUserUpdateDto(validDto));
    }

    @Test
    void givenAppUserUpdateDtoWithEmailAtMaxLength_whenValidateAppUserUpdateDto_thenDoesNotThrowException() {
        final AppUserUpdateDto validDto = AppUserUpdateDto.builder()
                .username("updateduser")
                .email("a".repeat(243) + "@example.com") //Length 255
                .build();
        assertDoesNotThrow(() -> validationService.validateAppUserUpdateDto(validDto));
    }

    @Test
    void givenNullUserId_whenValidateUserId_thenThrowsInvalidAppUserIDException() {
        assertThrows(InvalidAppUserIDException.class, () -> validationService.validateUserId(null));
    }

    @Test
    void givenZeroUserId_whenValidateUserId_thenThrowsInvalidAppUserIDException() {
        assertThrows(InvalidAppUserIDException.class, () -> validationService.validateUserId(0L));
    }

    @Test
    void givenNegativeUserId_whenValidateUserId_thenThrowsInvalidAppUserIDException() {
        assertThrows(InvalidAppUserIDException.class, () -> validationService.validateUserId(-5L));
    }

    @Test
    void givenValidUserId_whenValidateUserId_thenDoesNotThrowException() {
        assertDoesNotThrow(() -> validationService.validateUserId(1L));
        assertDoesNotThrow(() -> validationService.validateUserId(100L));
    }

    private static Stream<Arguments> providedInvalidLengthUsernameInputs() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("ab"),
                Arguments.of("abcdefghijklmnopqrstuvwxyz")
        );
    }

    private static Stream<Arguments> providedInvalidUsernameCharacterInputs() {
        return Stream.of(
                Arguments.of("test!"),
                Arguments.of("test#"),
                Arguments.of("test$"),
                Arguments.of("test ")
        );
    }

    private static Stream<Arguments> providedMissingPasswordCriteriaInputs() {
        return Stream.of(
                Arguments.of("Weak1!"),
                Arguments.of("Weak1a"),
                Arguments.of("WeakA1"),
                Arguments.of("WEAK1!"),
                Arguments.of("weak1!"),
                Arguments.of("Weak!")
        );
    }

    private static Stream<Arguments> providedInvalidEmailInputs() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("a@b"),
                Arguments.of("a".repeat(250) + "@example.com"),
                Arguments.of("invalid-email"),
                Arguments.of("test@example"),
                Arguments.of("@example.com"),
                Arguments.of("test@.com")
        );
    }
}

