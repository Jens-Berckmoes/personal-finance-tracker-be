package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.dto.TransactionCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.TransactionDto;

public interface TransactionService {
    TransactionDto addTransaction(final TransactionCreateDto transactionCreateDto);
}
