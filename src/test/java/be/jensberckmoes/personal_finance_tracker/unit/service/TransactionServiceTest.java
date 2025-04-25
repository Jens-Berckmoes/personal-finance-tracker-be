package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.dto.TransactionCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.TransactionDto;
import be.jensberckmoes.personal_finance_tracker.model.*;
import be.jensberckmoes.personal_finance_tracker.repository.AppUserRepository;
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
    private TransactionEntityMapper transactionEntityMapper;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private AppUser user;
    private Category category;
    private TransactionCreateDto transactionCreateDto;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = AppUser.builder()
                .id(1L)
                .username("TESTUSER")
                .password("TESTPASSWORD!123")
                .email("testuser@test.be")
                .role(Role.USER)
                .build();
        category = Category.builder()
                .id(1L)
                .name("TESTNAME")
                .build();
        transactionCreateDto = createTransactionCreateDto().build();
        transaction = createTransaction().build();
        when(appUserRepository.findById(transactionCreateDto.getUserId())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(transactionCreateDto.getCategoryId())).thenReturn(Optional.of(category));
    }

    @Test
    public void givenValidTransaction_whenAddTransactions_thenTransactionIsPersisted() {
        transaction = createTransactionWithOptionalFields().build();
        transactionCreateDto = createTransactionCreateDto()
                .method(TransactionMethod.BANK_TRANSFER)
                .description("Groceries")
                .build();
        final Transaction savedTransaction = createTransactionWithOptionalFields().id(1L).build();
        when(transactionRepository.save(transaction)).thenReturn(savedTransaction);
        when(transactionEntityMapper.fromDto(any(TransactionCreateDto.class))).thenReturn(transaction);
        final TransactionDto expectedDto = convertTransactionToDto(savedTransaction);
        when(transactionEntityMapper.toDto(any(Transaction.class))).thenReturn(expectedDto);
        final TransactionDto result = transactionService.addTransaction(transactionCreateDto);

        assertThat(result).usingRecursiveAssertion().isEqualTo(expectedDto);
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    public void givenValidTransactionWithOnlyNecessaryFields_whenAddTransactions_thenTransactionIsPersisted() {
        final Transaction savedTransaction = createTransaction().id(1L).build();
        when(transactionRepository.save(transaction)).thenReturn(savedTransaction);
        when(transactionEntityMapper.fromDto(any(TransactionCreateDto.class))).thenReturn(transaction);
        final TransactionDto expectedDto = convertTransactionToDto(savedTransaction);
        when(transactionEntityMapper.toDto(any(Transaction.class))).thenReturn(expectedDto);
        final TransactionDto result = transactionService.addTransaction(transactionCreateDto);

        assertThat(result).usingRecursiveAssertion().isEqualTo(expectedDto);
        verify(transactionRepository, times(1)).save(transaction);
    }

    private Transaction.TransactionBuilder createTransaction() {
        return Transaction.builder()
                .user(user)
                .category(category)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .date(LocalDateTime.of(LocalDate.of(2025, 4, 1), LocalTime.of(8, 40, 0)));
    }

    private TransactionCreateDto.TransactionCreateDtoBuilder createTransactionCreateDto() {
        return TransactionCreateDto.builder()
                .userId(user.getId())
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

    private TransactionDto convertTransactionToDto(final Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUser().getId());
        dto.setCategoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null);
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setDate(transaction.getDate());
        dto.setDescription(transaction.getDescription());
        dto.setMethod(transaction.getMethod());
        return dto;
    }
}
