package be.jensberckmoes.personal_finance_tracker.model;

import be.jensberckmoes.personal_finance_tracker.dto.TransactionRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.TransactionResponseDto;
import be.jensberckmoes.personal_finance_tracker.model.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TransactionMapper {
    public Optional<TransactionResponseDto> toResponse(final Transaction transaction) {
        return Optional.ofNullable(transaction)
                .map(e -> TransactionResponseDto.builder()
                        .id(transaction.getId())
                        .categoryId(transaction.getCategory().getId())
                        .date(transaction.getDate())
                        .method(transaction.getMethod())
                        .description(transaction.getDescription())
                        .amount(transaction.getAmount())
                        .type(transaction.getType())
                        .build());

    }

    public Optional<Transaction> toEntity(final TransactionRequestDto transactionRequestDto) {
        return Optional.ofNullable(transactionRequestDto)
                .map(dto -> Transaction.builder()
                        .amount(transactionRequestDto.getAmount())
                        .type(transactionRequestDto.getType())
                        .method(transactionRequestDto.getMethod())
                        .date(transactionRequestDto.getDate())
                        .description(transactionRequestDto.getDescription())
                        .build());

    }
}