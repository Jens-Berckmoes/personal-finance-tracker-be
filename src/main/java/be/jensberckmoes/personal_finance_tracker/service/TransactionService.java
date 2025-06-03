package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.dto.TransactionRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.TransactionResponseDto;

public interface TransactionService {
    TransactionResponseDto addTransaction(final TransactionRequestDto transactionRequestDto);
}
