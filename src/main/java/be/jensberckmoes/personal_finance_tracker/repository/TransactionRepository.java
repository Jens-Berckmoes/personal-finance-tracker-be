package be.jensberckmoes.personal_finance_tracker.repository;

import be.jensberckmoes.personal_finance_tracker.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
