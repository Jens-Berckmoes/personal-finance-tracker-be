package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.model.AppUser;
import be.jensberckmoes.personal_finance_tracker.model.Transaction;
import be.jensberckmoes.personal_finance_tracker.model.TransactionType;
import be.jensberckmoes.personal_finance_tracker.repository.TransactionRepository;
import be.jensberckmoes.personal_finance_tracker.service.TransactionServiceImpl;
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
    private TransactionServiceImpl transactionService;

    @Test
    public void givenValidTransaction_whenAddTransactions_thenTransactionIsPersisted() {
        final AppUser user = createUser().build();
        final Transaction transaction = createTransactionWithOptionalFields(user).build();
        final Transaction savedTransaction = createTransactionWithOptionalFields(user).id(1L).build();

        when(transactionRepository.save(transaction)).thenReturn(savedTransaction);

        final Transaction result = transactionService.addTransaction(transaction);

        assertEquals(savedTransaction, result);
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    public void givenValidTransactionWithOnlyNecessaryFields_whenAddTransactions_thenTransactionIsPersisted() {
        final AppUser user = createUser().build();
        final Transaction transaction = createTransaction(user).build();
        final Transaction savedTransaction = createTransaction(user).id(1L).build();

        when(transactionRepository.save(transaction)).thenReturn(savedTransaction);

        final Transaction result = transactionService.addTransaction(transaction);

        assertEquals(savedTransaction, result);
        verify(transactionRepository, times(1)).save(transaction);
    }

    private AppUser.AppUserBuilder createUser() {
        return AppUser.builder()
                .id(1L);
    }

    private Transaction.TransactionBuilder createTransaction(final AppUser user) {
        return Transaction.builder()
                .user(user)
                .amount(new BigDecimal("50.00"))
                .date(LocalDateTime.now());
    }

    private Transaction.TransactionBuilder createTransactionWithOptionalFields(final AppUser user) {
        return Transaction.builder()
                .user(user)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .date(LocalDateTime.now())
                .description("Groceries");
    }
}
