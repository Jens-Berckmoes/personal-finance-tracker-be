package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidAppUserNameException extends RuntimeException {
    public InvalidAppUserNameException(final String message) {
        super(message);
    }
}
