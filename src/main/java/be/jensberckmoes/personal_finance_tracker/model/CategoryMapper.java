package be.jensberckmoes.personal_finance_tracker.model;

import be.jensberckmoes.personal_finance_tracker.dto.CategoryRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.CategoryResponseDto;
import be.jensberckmoes.personal_finance_tracker.model.entity.Category;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class CategoryMapper {
    public Optional<CategoryResponseDto> toResponse(final Category category) {
        return Optional.ofNullable(category)
                .map(e -> CategoryResponseDto.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .categoryType(category.getCategoryType())
                        .categoryGroupType(category.getCategoryGroupType())
                        .build());
    }

    public Optional<Category> toEntity(final CategoryRequestDto categoryRequestDto) {
        return Optional.ofNullable(categoryRequestDto)
                .map(dto -> Category.builder()
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .categoryType(dto.getCategoryType())
                        .categoryGroupType(dto.getCategoryGroupType())
                        .build());
    }
}
