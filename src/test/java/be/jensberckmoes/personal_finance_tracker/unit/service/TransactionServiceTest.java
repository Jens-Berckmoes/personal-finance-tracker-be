package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.model.AppUser;
import be.jensberckmoes.personal_finance_tracker.model.Transaction;
import be.jensberckmoes.personal_finance_tracker.model.TransactionType;
import be.jensberckmoes.personal_finance_tracker.repository.TransactionRepository;
import be.jensberckmoes.personal_finance_tracker.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void testAddTransaction_ShouldSaveTransactionAndReturnSavedTransaction() {
        final AppUser user = new AppUser();
        user.setId(1L);
        final Transaction transaction = Transaction.builder()
                .user(user)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .date(LocalDateTime.now())
                .description("Groceries")
                .build();

        final Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .user(user)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .date(LocalDateTime.now())
                .description("Groceries")
                .build();

        when(transactionRepository.save(transaction)).thenReturn(savedTransaction);

        final Transaction result = transactionService.addTransaction(transaction);

        assertEquals(savedTransaction, result);
        verify(transactionRepository, times(1)).save(transaction);
    }
}
