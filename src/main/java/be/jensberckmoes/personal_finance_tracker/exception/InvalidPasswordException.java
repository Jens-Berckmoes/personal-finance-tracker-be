package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidPasswordException extends RuntimeException{
    public InvalidPasswordException(final String message) {
        super(message);
    }
}
