package be.jensberckmoes.personal_finance_tracker.dto;

import be.jensberckmoes.personal_finance_tracker.model.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequestDto {
    @NotBlank
    @Size(max = 50, message = "Category name cannot exceed 50 characters")
    private String name;
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    @NotNull
    private CategoryType categoryType;
    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 100, message = "Category Group Type cannot exceed 100 characters")
    private String categoryGroupType;
}
