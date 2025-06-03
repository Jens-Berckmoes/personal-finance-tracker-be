package be.jensberckmoes.personal_finance_tracker.model;

import be.jensberckmoes.personal_finance_tracker.dto.TransactionRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.TransactionResponseDto;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    public TransactionResponseDto toDto(final Transaction transaction) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .categoryId(transaction.getCategory().getId())
                .date(transaction.getDate())
                .method(transaction.getMethod())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .build();
    }

    public Transaction fromDto(final TransactionRequestDto transactionRequestDto) {
        return Transaction.builder()
                .amount(transactionRequestDto.getAmount())
                .type(transactionRequestDto.getType())
                .method(transactionRequestDto.getMethod())
                .date(transactionRequestDto.getDate())
                .description(transactionRequestDto.getDescription())
                .build();
    }
}