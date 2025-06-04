package be.jensberckmoes.personal_finance_tracker.model.entity;

import be.jensberckmoes.personal_finance_tracker.model.enums.TransactionMethod;
import be.jensberckmoes.personal_finance_tracker.model.enums.TransactionType;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId")
    private Category category;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "type")
    private TransactionType type;

    @Column(name = "method")
    private TransactionMethod method;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "description")
    private String description;
}