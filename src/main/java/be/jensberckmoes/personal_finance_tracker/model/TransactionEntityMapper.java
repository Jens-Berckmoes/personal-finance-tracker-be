package be.jensberckmoes.personal_finance_tracker.model;

import be.jensberckmoes.personal_finance_tracker.dto.TransactionCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.TransactionDto;
import org.springframework.stereotype.Component;

@Component
public class TransactionEntityMapper {
    public TransactionDto toDto(final Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .categoryId(transaction.getCategory().getId())
                .userId(transaction.getUser().getId())
                .date(transaction.getDate())
                .method(transaction.getMethod())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .build();
    }

    public Transaction fromDto(final TransactionCreateDto transactionCreateDto) {
        return Transaction.builder()
                .amount(transactionCreateDto.getAmount())
                .type(transactionCreateDto.getType())
                .method(transactionCreateDto.getMethod())
                .date(transactionCreateDto.getDate())
                .description(transactionCreateDto.getDescription())
                .build();
    }
}