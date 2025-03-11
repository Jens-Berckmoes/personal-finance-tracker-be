package be.jensberckmoes.personal_finance_tracker.exception;

public class BlankParameterException extends RuntimeException {
    public BlankParameterException(String message) {
        super(message);
    }
}
