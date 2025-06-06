package be.jensberckmoes.personal_finance_tracker.service.impl;

import be.jensberckmoes.personal_finance_tracker.dto.TransactionRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.TransactionResponseDto;
import be.jensberckmoes.personal_finance_tracker.exception.InvalidInputException;
import be.jensberckmoes.personal_finance_tracker.model.entity.Category;
import be.jensberckmoes.personal_finance_tracker.model.entity.Transaction;
import be.jensberckmoes.personal_finance_tracker.model.TransactionMapper;
import be.jensberckmoes.personal_finance_tracker.repository.CategoryRepository;
import be.jensberckmoes.personal_finance_tracker.repository.TransactionRepository;
import be.jensberckmoes.personal_finance_tracker.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final CategoryRepository categoryRepository;

    @Override
    public TransactionResponseDto addTransaction(final TransactionRequestDto transactionRequestDto) {
        final Transaction transaction = transactionMapper.toEntity(transactionRequestDto).orElseThrow(() -> new InvalidInputException("Invalid TransactionRequestDto received. Null object"));
        final Optional<Category> optionalCategory = categoryRepository.findById(transactionRequestDto.getCategoryId());
        optionalCategory.ifPresent(transaction::setCategory);
        final Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toResponse(savedTransaction).orElseThrow(() -> new InvalidInputException("Invalid Category entity received from database. (object is null)."));
    }

}
