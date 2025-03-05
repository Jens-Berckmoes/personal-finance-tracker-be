package be.jensberckmoes.personal_finance_tracker.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ValidationServiceTest {

    @Autowired
    private ValidationService validationService;

    @ParameterizedTest
    @MethodSource("providedValidEmails")
    public void testValidEmails(final String email, final boolean expectedResult) {
        assertEquals(expectedResult, validationService.isValidEmail(email));
    }

    @ParameterizedTest
    @MethodSource("providedEmailsMissingComponents")
    public void testEmailsWithMissingComponents(final String email, final boolean expectedResult) {
        assertEquals(expectedResult, validationService.isValidEmail(email));
    }

    @ParameterizedTest
    @MethodSource("providedEmailsForLengthBoundaries")
    public void testEmailsLengthBoundaries(final String email, final boolean expectedResult) {
        assertEquals(expectedResult, validationService.isValidEmail(email));
    }

    @ParameterizedTest
    @MethodSource("providedEmailsWithInvalidCharacters")
    public void testEmailsWithInvalidCharacters(final String email, final boolean expectedResult) {
        assertEquals(expectedResult, validationService.isValidEmail(email));
    }

    @ParameterizedTest
    @MethodSource("providedEmailsForNullOrEmptyValidation")
    public void testNullOrEmptyEmails(final String email, final boolean expectedResult) {
        assertEquals(expectedResult, validationService.isValidEmail(email));
    }

    @ParameterizedTest
    @MethodSource("providedValidPasswordsForValidation")
    public void testPasswordsForValidCases(final String password, final boolean expectedResult) {
        assertEquals(expectedResult, validationService.isValidPassword(password));
    }

    @ParameterizedTest
    @MethodSource("providedPasswordsForLengthBoundaries")
    public void testPasswordsLengthBoundaries(final String password, final boolean expectedResult) {
        assertEquals(expectedResult, validationService.isValidPassword(password));
    }

    @ParameterizedTest
    @MethodSource("providedPasswordsForMissingCharacterValidation")
    public void testPasswordsWithMissingComponents(final String password, final boolean expectedResult) {
        assertEquals(expectedResult, validationService.isValidPassword(password));
    }

    @ParameterizedTest
    @MethodSource("providedPasswordsForSpecialCharactersValidation")
    public void testPasswordsWithSpecialCharacters(final String password, final boolean expectedResult) {
        assertEquals(expectedResult, validationService.isValidPassword(password));
    }

    @ParameterizedTest
    @MethodSource("providedPasswordsForNullOrEmptyValidation")
    public void testPasswordsForNullOrEmptyValues(final String password, final boolean expectedResult) {
        assertEquals(expectedResult, validationService.isValidPassword(password));
    }

    @ParameterizedTest
    @MethodSource("providedUsernamesForCharacterValidation")
    public void testUsernames(final String username, final boolean expectedResult) {
        assertEquals(expectedResult, validationService.isValidUsername(username));
    }

    @ParameterizedTest
    @MethodSource("providedUsernamesForLengthBoundaries")
    public void testUsernameLengthBoundaries(final String username, final boolean expectedResult) {
        assertEquals(expectedResult, validationService.isValidUsername(username));
    }

    @ParameterizedTest
    @MethodSource("providedUsernamesForNullOrEmptyValidation")
    public void testNullOrEmptyUsernames(final String username, final boolean expectedResult) {
        assertEquals(expectedResult, validationService.isValidUsername(username));
    }

    private static Stream<Arguments> providedValidEmails() {
        return Stream.of(
                Arguments.of("test@example.com", true),                             // Valid e-mail.
                Arguments.of("user@sub.example.com", true),                         // Uncommon, but valid.
                Arguments.of("a_very_long_email_address_user@example.com", true)    // Very long, but valid.
        );
    }

    private static Stream<Arguments> providedEmailsMissingComponents() {
        return Stream.of(
                Arguments.of("invalid-email", false),           // Not even an e-mail.
                Arguments.of("@example.com", false),            // Missing Local Part Of e-mail.
                Arguments.of("somethingexample.com", false),    // Missing @-character.
                Arguments.of("something@.com", false),          // No domain.
                Arguments.of("something@example.", false)       // Missing Top Level Domain.
        );
    }

    private static Stream<Arguments> providedEmailsForLengthBoundaries() {
        return Stream.of(
                Arguments.of(getEmailString(65, 255, 63), false),    // Local part of e-mail has too many characters. Limited to 64.
                Arguments.of(getEmailString(64, 256, 63), false),    // Domain part of e-mail has too many characters. Limited to 255.
                Arguments.of(getEmailString(64, 255, 64), false),    // Top level domain part of e-mail has too many characters. Limited to 63.
                Arguments.of(getEmailString(65, 256, 63), false),    // Local part and domain part of e-mail has too many characters. Limited to 64 and 255.
                Arguments.of(getEmailString(65, 255, 64), false),    // Local part and top level domain part of e-mail has too many characters. Limited to 64 and 63.
                Arguments.of(getEmailString(64, 256, 64), false)     // Domain part and top level domain part of Email has too many characters. Limited to 255 and 63.
        );
    }

    private static Stream<Arguments> providedEmailsWithInvalidCharacters() {
        return Stream.of(
                Arguments.of("examplecom", false),         // Missing dot between domains.
                Arguments.of("user@exämple.com", false),   // International letter, not allowed.
                Arguments.of("user@exam_ple.com", false)   // Underscore in domain (not allowed in domain names)
        );
    }

    private static Stream<Arguments> providedEmailsForNullOrEmptyValidation() {
        return Stream.of(
                Arguments.of("", false),     // Empty input
                Arguments.of(null, false)    // Null e-mail
        );
    }

    private static Stream<Arguments> providedValidPasswordsForValidation() {
        return Stream.of(
                Arguments.of("Ab!456789101", true),
                Arguments.of("Ab!456789!*-_.", true),    // Valid with all allowed special characters
                Arguments.of("!Ab1_12345678", true),     // Valid even with trailing special characters.
                Arguments.of("Ab1_12345678!", true) ,    // Valid even with trailing special characters.
                Arguments.of("Ab!!1234567!!", true)        // Multiple allowed special characters
        );
    }

    private static Stream<Arguments> providedPasswordsForLengthBoundaries() {
        return Stream.of(
                Arguments.of("Ab!1" + "1".repeat(2), false),    // Less than twelve characters
                Arguments.of("Ab!1" + "1".repeat(61), false),   // More than 64 characters
                Arguments.of("Ab!1" + "1".repeat(8), true),     // Exactly 12 (minimum)
                Arguments.of("Ab!1" + "1".repeat(60), true)     // Exactly 64 (maximum)
        );
    }

    private static Stream<Arguments> providedPasswordsForMissingCharacterValidation() {
        return Stream.of(
                Arguments.of("bb!456789101", false),    // No uppercase
                Arguments.of("AB!456789101", false),    // No lowercase
                Arguments.of("Ab!cdefghijk", false)     // No number
        );
    }

    private static Stream<Arguments> providedPasswordsForSpecialCharactersValidation() {
        final Stream<Arguments> allowedCharTests = "!*-_."
                .chars()
                .mapToObj(c -> Arguments.of("Ab" + (char) c + "1".repeat(13), true)); // Valid for each allowed special char

        final Stream<Arguments> disallowedCharTests = "%@&^#"
                .chars()
                .mapToObj(c -> Arguments.of("Ab" + (char) c + "1".repeat(13), false)); // Invalid for each disallowed char

        return Stream.concat(
                Stream.concat(allowedCharTests, disallowedCharTests),
                Stream.of(Arguments.of("Ab@#1234567", false)));
    }


    private static Stream<Arguments> providedPasswordsForNullOrEmptyValidation() {
        return Stream.of(
                Arguments.of("", false),     // Empty input
                Arguments.of(null, false)    // Null password
        );
    }

    private static Stream<Arguments> providedUsernamesForCharacterValidation() {
        return Stream.of(
                Arguments.of("testuser", true),
                Arguments.of("test@user", false)    // Special character used, not allowed.
        );
    }

    private static Stream<Arguments> providedUsernamesForLengthBoundaries() {
        return Stream.of(
                Arguments.of("a".repeat(20), true),     // Maximum length of 20
                Arguments.of("a".repeat(21), false),    // Exceeds maximum length
                Arguments.of("abc", true),                     // Minimum length of 3
                Arguments.of("a".repeat(2), false)      // Less than 3 length
        );
    }

    private static Stream<Arguments> providedUsernamesForNullOrEmptyValidation() {
        return Stream.of(
                Arguments.of("", false),     // Empty input
                Arguments.of(null, false)    // Null username
        );
    }

    private static String getEmailString(final int amountOfLocalPart,
                                         final int amountOfDomainPart,
                                         final int amountOfTopLevelDomain) {
        return "a".repeat(amountOfLocalPart) + "@" + "b".repeat(amountOfDomainPart) + "." + "c".repeat(amountOfTopLevelDomain);
    }

}

