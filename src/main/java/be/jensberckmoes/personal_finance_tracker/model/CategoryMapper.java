package be.jensberckmoes.personal_finance_tracker.model;

import be.jensberckmoes.personal_finance_tracker.dto.CategoryRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.CategoryResponseDto;
import be.jensberckmoes.personal_finance_tracker.model.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryResponseDto toResponse(final Category category) {
        return CategoryResponseDto
                .builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .categoryType(category.getCategoryType())
                .categoryGroupType(category.getCategoryGroupType())
                .build();
    }

    public Category toEntity(final CategoryRequestDto categoryRequestDto) {
        return Category
                .builder()
                .name(categoryRequestDto.getName())
                .description(categoryRequestDto.getDescription())
                .categoryType(categoryRequestDto.getCategoryType())
                .categoryGroupType(categoryRequestDto.getCategoryGroupType())
                .build();
    }
}
