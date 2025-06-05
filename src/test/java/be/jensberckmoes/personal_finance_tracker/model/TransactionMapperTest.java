package be.jensberckmoes.personal_finance_tracker.model;

import be.jensberckmoes.personal_finance_tracker.dto.TransactionRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.TransactionResponseDto;
import be.jensberckmoes.personal_finance_tracker.model.entity.Category;
import be.jensberckmoes.personal_finance_tracker.model.entity.Transaction;
import be.jensberckmoes.personal_finance_tracker.model.enums.TransactionMethod;
import be.jensberckmoes.personal_finance_tracker.model.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionMapperTest {

    private TransactionMapper transactionMapper;

    @BeforeEach
    void setUp() {
        transactionMapper = new TransactionMapper();
    }

    @Test
    @DisplayName("Should map Transaction entity to TransactionResponseDto successfully")
    void givenTransaction_whenToResponse_thenReturnsCorrectDto() {
        // Given
        final Category category = Category.builder()
                .id(101L)
                .name("Groceries")
                .build();
        final Transaction transaction = Transaction.builder()
                .id(1L)
                .category(category)
                .date(LocalDateTime.of(2023, 1, 15,8,11,12))
                .method(TransactionMethod.DIRECT_DEBIT)
                .description("Weekly grocery shopping")
                .amount(new BigDecimal("75.50"))
                .type(TransactionType.EXPENSE)
                .build();

        // When
        final Optional<TransactionResponseDto> optionalDto = transactionMapper.toResponse(transaction);

        // Then
        assertThat(optionalDto).isPresent();
        final TransactionResponseDto dto = optionalDto.get();
        assertThat(dto.getId()).isEqualTo(transaction.getId());
        assertThat(dto.getCategoryId()).isEqualTo(transaction.getCategory().getId());
        assertThat(dto.getDate()).isEqualTo(transaction.getDate());
        assertThat(dto.getMethod()).isEqualTo(transaction.getMethod());
        assertThat(dto.getDescription()).isEqualTo(transaction.getDescription());
        assertThat(dto.getAmount()).isEqualTo(transaction.getAmount());
        assertThat(dto.getType()).isEqualTo(transaction.getType());
    }

    @Test
    @DisplayName("Should throw NullPointerException when mapping Transaction with null Category to TransactionResponseDto")
    void givenTransactionWithNullCategory_whenToResponse_thenThrowsNullPointerException() {
        // Given
        final Transaction transaction = Transaction.builder()
                .id(1L)
                .category(null) // Category is null
                .date(LocalDateTime.of(2023, 1, 15,8,11,12))
                .method(TransactionMethod.DIRECT_DEBIT)
                .description("Weekly grocery shopping")
                .amount(new BigDecimal("75.50"))
                .type(TransactionType.EXPENSE)
                .build();

        // When / Then
        assertThrows(NullPointerException.class, () -> transactionMapper.toResponse(transaction),
                "Should throw NullPointerException when category is null");
    }

    @Test
    @DisplayName("Should return empty Optional when mapping null Transaction to TransactionResponseDto")
    void givenNullTransaction_whenToResponse_thenReturnsEmptyOptional() {
        // Given
        final Transaction transaction = null;

        // When
        final Optional<TransactionResponseDto> optionalDto = transactionMapper.toResponse(transaction);

        // Then
        assertThat(optionalDto).isEmpty();
    }

    @Test
    @DisplayName("Should map TransactionRequestDto to Transaction entity successfully")
    void givenTransactionRequestDto_whenToEntity_thenReturnsCorrectTransaction() {
        // Given
        final TransactionRequestDto dto = TransactionRequestDto.builder()
                .categoryId(101L)
                .date(LocalDateTime.of(2023, 2, 20,8,11,12))
                .method(TransactionMethod.CREDIT_CARD)
                .description("Online subscription")
                .amount(new BigDecimal("12.99"))
                .type(TransactionType.INCOME) // Changed to INCOME for variety
                .build();

        // When
        final Optional<Transaction> optionalTransaction = transactionMapper.toEntity(dto);

        // Then
        assertThat(optionalTransaction).isPresent();
        final Transaction transaction = optionalTransaction.get();
        assertThat(transaction.getId()).isNull(); // ID is not set by request DTO
        assertThat(transaction.getCategory()).isNull(); // Category object not set by request DTO
        assertThat(transaction.getDate()).isEqualTo(dto.getDate());
        assertThat(transaction.getMethod()).isEqualTo(dto.getMethod());
        assertThat(transaction.getDescription()).isEqualTo(dto.getDescription());
        assertThat(transaction.getAmount()).isEqualTo(dto.getAmount());
        assertThat(transaction.getType()).isEqualTo(dto.getType());
    }

    @Test
    @DisplayName("Should return empty Optional when mapping null TransactionRequestDto to Transaction entity")
    void givenNullTransactionRequestDto_whenToEntity_thenReturnsEmptyOptional() {
        // Given
        final TransactionRequestDto dto = null;

        // When
        final Optional<Transaction> optionalTransaction = transactionMapper.toEntity(dto);

        // Then
        assertThat(optionalTransaction).isEmpty();
    }

    @Test
    @DisplayName("Should map Transaction with null description to DTO with null description")
    void givenTransactionWithNullDescription_whenToResponse_thenDescriptionIsNullInDto() {
        // Given
        final Category category = Category.builder().id(102L).name("Utilities").build();
        final Transaction transaction = Transaction.builder()
                .id(2L)
                .category(category)
                .date(LocalDateTime.of(2023, 3, 1,8,11,12))
                .method(TransactionMethod.DIRECT_DEBIT)
                .description(null)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .build();

        // When
        final Optional<TransactionResponseDto> optionalDto = transactionMapper.toResponse(transaction);

        // Then
        assertThat(optionalDto).isPresent();
        final TransactionResponseDto dto = optionalDto.get();
        assertThat(dto.getDescription()).isNull();
    }

    @Test
    @DisplayName("Should map TransactionRequestDto with null description to Entity with null description")
    void givenTransactionRequestDtoWithNullDescription_whenToEntity_thenDescriptionIsNullInEntity() {
        // Given
        final TransactionRequestDto dto = TransactionRequestDto.builder()
                .categoryId(103L)
                .date(LocalDateTime.of(2023, 4, 5,8,11,12))
                .method(TransactionMethod.CASH)
                .description(null)
                .amount(new BigDecimal("20.00"))
                .type(TransactionType.EXPENSE)
                .build();

        // When
        final Optional<Transaction> optionalTransaction = transactionMapper.toEntity(dto);

        // Then
        assertThat(optionalTransaction).isPresent();
        final Transaction transaction = optionalTransaction.get();
        assertThat(transaction.getDescription()).isNull();
    }
}
