package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidRoleException extends RuntimeException{
    public InvalidRoleException(final String message) {
        super(message);
    }
}
