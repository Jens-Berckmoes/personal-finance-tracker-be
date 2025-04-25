package be.jensberckmoes.personal_finance_tracker.exception;

public class InvalidTransactionAmountException extends RuntimeException {
    public InvalidTransactionAmountException(final String msg) {
        super(msg);
    }
}
