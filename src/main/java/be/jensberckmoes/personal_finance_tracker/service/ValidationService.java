package be.jensberckmoes.personal_finance_tracker.service;

public interface ValidationService {

    boolean isValidEmail(final String email);

    boolean isValidPassword(final String password);

    boolean isValidUsername(final String username);

}
