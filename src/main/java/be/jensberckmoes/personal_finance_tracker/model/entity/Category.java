package be.jensberckmoes.personal_finance_tracker.model.entity;

import be.jensberckmoes.personal_finance_tracker.model.enums.CategoryType;
import jakarta.persistence.*;
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
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 50, message = "Category name cannot exceed 50 characters")
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING) 
    @Column(name = "category_type", nullable = false, length = 100)
    @NotNull
    private CategoryType categoryType;

    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 100, message = "Category Group Type cannot exceed 100 characters")
    @Column(name = "category_group_type", nullable = false, length = 100)
    private String categoryGroupType;
}