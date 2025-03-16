package be.jensberckmoes.personal_finance_tracker.exception;

public class AppUserNotFoundException extends RuntimeException {
    public AppUserNotFoundException(final String message) {
        super(message);
    }
}
