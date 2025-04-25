package be.jensberckmoes.personal_finance_tracker.service.impl;

import be.jensberckmoes.personal_finance_tracker.dto.TransactionCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.TransactionDto;
import be.jensberckmoes.personal_finance_tracker.model.AppUser;
import be.jensberckmoes.personal_finance_tracker.model.Category;
import be.jensberckmoes.personal_finance_tracker.model.Transaction;
import be.jensberckmoes.personal_finance_tracker.model.TransactionEntityMapper;
import be.jensberckmoes.personal_finance_tracker.repository.AppUserRepository;
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
    private final TransactionEntityMapper transactionEntityMapper;
    private final CategoryRepository categoryRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public TransactionDto addTransaction(final TransactionCreateDto transactionCreateDto) {
        final Transaction transaction = transactionEntityMapper.fromDto(transactionCreateDto);
        final Optional<AppUser> optionalAppUser = appUserRepository.findById(transactionCreateDto.getUserId());
        final Optional<Category> optionalCategory = categoryRepository.findById(transactionCreateDto.getCategoryId());
        optionalAppUser.ifPresent(transaction::setUser);
        optionalCategory.ifPresent(transaction::setCategory);
        final Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionEntityMapper.toDto(savedTransaction);
    }

}
