package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.model.Transaction;

public interface TransactionService {
    Transaction addTransaction(final Transaction transaction);
}
