package be.jensberckmoes.personal_finance_tracker.exception;

public class NullParameterException extends RuntimeException {
    public NullParameterException(final String message) {
        super(message);
    }
}