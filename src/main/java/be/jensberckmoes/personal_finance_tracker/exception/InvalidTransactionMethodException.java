package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidTransactionMethodException extends RuntimeException {
    public InvalidTransactionMethodException(final String msg) {
        super(msg);
    }
}
