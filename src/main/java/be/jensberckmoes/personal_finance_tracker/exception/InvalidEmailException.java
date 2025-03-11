package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(final String message) {
        super(message);
    }
}
