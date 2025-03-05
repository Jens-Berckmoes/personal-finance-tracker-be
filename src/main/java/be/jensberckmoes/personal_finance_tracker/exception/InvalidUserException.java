package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidUserException extends RuntimeException {
    public InvalidUserException(final String message) {
        super(message);
    }
}
