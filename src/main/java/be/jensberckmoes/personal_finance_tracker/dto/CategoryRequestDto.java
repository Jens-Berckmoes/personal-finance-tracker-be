package be.jensberckmoes.personal_finance_tracker.dto;

import be.jensberckmoes.personal_finance_tracker.model.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequestDto {
    private String name;
    private String description;
    private CategoryType categoryType;
    private String categoryGroupType;
}
