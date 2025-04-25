package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.dto.AppUserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserUpdateDto;
import be.jensberckmoes.personal_finance_tracker.exception.*;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AppUserValidationService {

    public void validateAppUserCreateDto(final AppUserCreateDto appUserCreateDto) {
        validateNotNull(appUserCreateDto);
        validateUsername(appUserCreateDto.getUsername());
        validatePassword(appUserCreateDto.getPassword());
        validateEmail(appUserCreateDto.getEmail());
    }
    public void validateAppUserUpdateDto(final AppUserUpdateDto appUserUpdateDto) {
        validateNotNull(appUserUpdateDto);
        validateUsername(appUserUpdateDto.getUsername());
        validateEmail(appUserUpdateDto.getEmail());
    }

    private static void validateNotNull(final AppUserCreateDto appUserCreateDto) {
        if (Objects.isNull(appUserCreateDto)) {
            throw new NullParameterException("Parameter 'userCreateDto' cannot be null");
        }
    }
    private static void validateNotNull(final AppUserUpdateDto appUserUpdateDto) {
        if (Objects.isNull(appUserUpdateDto)) {
            throw new NullParameterException("Parameter 'userUpdateDto' cannot be null");
        }
    }

    private void validateEmail(final String email) {
        final String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,255}$";
        if (Objects.isNull(email)) {
            throw new InvalidEmailException("Email can not be null.");
        }
        if (email.isEmpty()) {
            throw new InvalidEmailException("Email can not be empty.");
        }

        if (email.length() > 255 || email.length() < 2) {
            throw new InvalidEmailException("Email length must be between 2 and 255 characters long.");
        }
        if (!email.matches(emailRegex)) {
            throw new InvalidEmailException("Email must be in the form (test@example.com).");
        }
    }

    private void validatePassword(final String password) {
        final String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!.*_-])[A-Za-z\\d!.*_-]{12,255}$";
        if (Objects.isNull(password)) {
            throw new InvalidPasswordException("Password can not be null.");
        }
        if (password.isBlank()) {
            throw new InvalidPasswordException("Password can not be empty.");
        }
        if (password.length() < 12 || password.length() > 255) {
            throw new InvalidPasswordException("Password length must be between 12 and 255 characters long.");
        }
        if (!password.matches(passwordRegex)) {
            throw new InvalidPasswordException("Password must contain 1 uppercase, 1 lowercase, 1 number and 1 special character(!.*_-).");
        }
    }

    public void validateUsername(final String username) {
        final String usernameRegex = "^[a-zA-Z0-9._]{3,20}$";
        if (Objects.isNull(username)) {
            throw new InvalidAppUserNameException("Username can not be null.");
        }
        final String nonCappedUsername = username.toLowerCase();
        if (nonCappedUsername.isBlank()) {
            throw new InvalidAppUserNameException("Username can not be empty.");
        }
        if (nonCappedUsername.length() < 3 || username.length() > 20) {
            throw new InvalidAppUserNameException("Username length must be between 3 and 20 characters long.");
        }
        if (!nonCappedUsername.matches(usernameRegex)) {
            throw new InvalidAppUserNameException("Username may contain only letters and numbers.");
        }
    }
    public void validateUserId(final Long id){
        if (Objects.isNull(id) || id <= 0) {
            throw new InvalidAppUserIDException("ID must be a positive number");
        }
    }

}
