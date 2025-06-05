package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidInputException extends RuntimeException {
    public InvalidInputException(final String message) {
        super(message);
    }
}
