package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidUserIDException extends RuntimeException {
    public InvalidUserIDException(final String message) {
        super(message);
    }
}
