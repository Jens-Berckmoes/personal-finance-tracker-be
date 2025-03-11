package be.jensberckmoes.personal_finance_tracker.exception;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(final String message) {
        super(message);
    }
}
