package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidAppUserIDException extends RuntimeException {
    public InvalidAppUserIDException(final String message) {
        super(message);
    }
}
