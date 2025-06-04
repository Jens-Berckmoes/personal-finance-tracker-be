package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.dto.TransactionRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.TransactionResponseDto;
import be.jensberckmoes.personal_finance_tracker.model.*;
import be.jensberckmoes.personal_finance_tracker.model.entity.Category;
import be.jensberckmoes.personal_finance_tracker.model.entity.Transaction;
import be.jensberckmoes.personal_finance_tracker.model.enums.TransactionMethod;
import be.jensberckmoes.personal_finance_tracker.model.enums.TransactionType;
import be.jensberckmoes.personal_finance_tracker.repository.CategoryRepository;
import be.jensberckmoes.personal_finance_tracker.repository.TransactionRepository;
import be.jensberckmoes.personal_finance_tracker.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Category category;
    private TransactionRequestDto transactionRequestDto;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("TESTNAME")
                .build();
        transactionRequestDto = createTransactionCreateDto().build();
        transaction = createTransaction().build();
        when(categoryRepository.findById(transactionRequestDto.getCategoryId())).thenReturn(Optional.of(category));
    }

    @Test
    public void givenValidTransaction_whenAddTransactions_thenTransactionIsPersisted() {
        transaction = createTransactionWithOptionalFields().build();
        transactionRequestDto = createTransactionCreateDto()
                .method(TransactionMethod.BANK_TRANSFER)
                .description("Groceries")
                .build();
        final Transaction savedTransaction = createTransactionWithOptionalFields().id(1L).build();
        when(transactionRepository.save(transaction)).thenReturn(savedTransaction);
        when(transactionMapper.fromDto(any(TransactionRequestDto.class))).thenReturn(transaction);
        final TransactionResponseDto expectedDto = convertTransactionToDto(savedTransaction);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(expectedDto);
        final TransactionResponseDto result = transactionService.addTransaction(transactionRequestDto);

        assertThat(result).usingRecursiveAssertion().isEqualTo(expectedDto);
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    public void givenValidTransactionWithOnlyNecessaryFields_whenAddTransactions_thenTransactionIsPersisted() {
        final Transaction savedTransaction = createTransaction().id(1L).build();
        when(transactionRepository.save(transaction)).thenReturn(savedTransaction);
        when(transactionMapper.fromDto(any(TransactionRequestDto.class))).thenReturn(transaction);
        final TransactionResponseDto expectedDto = convertTransactionToDto(savedTransaction);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(expectedDto);
        final TransactionResponseDto result = transactionService.addTransaction(transactionRequestDto);

        assertThat(result).usingRecursiveAssertion().isEqualTo(expectedDto);
        verify(transactionRepository, times(1)).save(transaction);
    }

    private Transaction.TransactionBuilder createTransaction() {
        return Transaction.builder()
                .category(category)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .date(LocalDateTime.of(LocalDate.of(2025, 4, 1), LocalTime.of(8, 40, 0)));
    }

    private TransactionRequestDto.TransactionRequestDtoBuilder createTransactionCreateDto() {
        return TransactionRequestDto.builder()
                .categoryId(category.getId())
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .date(LocalDateTime.of(LocalDate.of(2025, 4, 1), LocalTime.of(8, 40, 0)));
    }

    private Transaction.TransactionBuilder createTransactionWithOptionalFields() {
        return createTransaction()
                .method(TransactionMethod.BANK_TRANSFER)
                .description("Groceries");
    }

    private TransactionResponseDto convertTransactionToDto(final Transaction transaction) {
        TransactionResponseDto dto = new TransactionResponseDto();
        dto.setId(transaction.getId());
        dto.setCategoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null);
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setDate(transaction.getDate());
        dto.setDescription(transaction.getDescription());
        dto.setMethod(transaction.getMethod());
        return dto;
    }
}
