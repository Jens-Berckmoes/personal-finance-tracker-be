package be.jensberckmoes.personal_finance_tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private AppUser user;

    private Category category;

    private BigDecimal amount;

    private TransactionType type;

    private TransactionMethod paymentMethod;

    private LocalDateTime date;

    private String description;
}