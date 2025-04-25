package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidTransactionDescriptionException extends RuntimeException {
    public InvalidTransactionDescriptionException(final String msg) {
        super(msg);
    }
}
