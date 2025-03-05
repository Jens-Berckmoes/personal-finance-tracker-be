package be.jensberckmoes.personal_finance_tracker.service;

import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ValidationServiceImpl implements ValidationService {

    @Override
    public boolean isValidEmail(final String email) {
        if (Objects.isNull(email) || email.isEmpty() || email.length() > 254) {
            return false;
        }
        final String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    @Override
    public boolean isValidPassword(final String password) {
        if (Objects.isNull(password) || password.isBlank() || password.length() < 12 || password.length() > 64) {
            return false;
        }
        final String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!.*_-])[A-Za-z\\d!.*_-]{12,64}$";
        return password.matches(passwordRegex);
    }

    @Override
    public boolean isValidUsername(final String username) {
        if (Objects.isNull(username) || username.isBlank() || username.length() < 3 || username.length() > 20) {
            return false;
        }
        final String usernameRegex = "^[a-zA-Z0-9._]{3,20}$";
        return username.matches(usernameRegex);
    }

}
