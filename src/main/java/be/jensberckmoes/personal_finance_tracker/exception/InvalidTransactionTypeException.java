package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidTransactionTypeException extends RuntimeException {
    public InvalidTransactionTypeException(final String msg) {
        super(msg);
    }
}
