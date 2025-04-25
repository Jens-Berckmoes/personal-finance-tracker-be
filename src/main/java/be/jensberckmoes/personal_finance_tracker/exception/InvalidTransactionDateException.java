package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidTransactionDateException extends RuntimeException {
    public InvalidTransactionDateException(final String msg) {
        super(msg);
    }
}
