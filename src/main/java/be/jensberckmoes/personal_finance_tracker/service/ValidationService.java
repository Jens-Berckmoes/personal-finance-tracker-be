package be.jensberckmoes.personal_finance_tracker.service;

public interface ValidationService {

    boolean isValidEmail(final String email);
    boolean isValidUsername(final String username);

}
