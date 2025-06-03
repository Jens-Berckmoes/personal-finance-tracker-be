package be.jensberckmoes.personal_finance_tracker.dto;

import be.jensberckmoes.personal_finance_tracker.model.TransactionMethod;
import be.jensberckmoes.personal_finance_tracker.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponseDto {
    private Long id;
    private Long userId;
    private Long categoryId;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionMethod method;
    private LocalDateTime date;
    private String description;
}
