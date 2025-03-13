package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidAppUserException extends RuntimeException {
    public InvalidAppUserException(final String message) {
        super(message);
    }
}
