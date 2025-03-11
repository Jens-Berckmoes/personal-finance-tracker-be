package be.jensberckmoes.personal_finance_tracker.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(final String message) {
        super(message);
    }
}