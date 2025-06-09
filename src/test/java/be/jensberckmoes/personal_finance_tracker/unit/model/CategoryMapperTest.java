package be.jensberckmoes.personal_finance_tracker.unit.service.model;

import be.jensberckmoes.personal_finance_tracker.dto.CategoryRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.CategoryResponseDto;
import be.jensberckmoes.personal_finance_tracker.model.CategoryMapper;
import be.jensberckmoes.personal_finance_tracker.model.entity.Category;
import be.jensberckmoes.personal_finance_tracker.model.enums.CategoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

    private CategoryMapper categoryMapper;

    @BeforeEach
    void setUp() {
        categoryMapper = new CategoryMapper();
    }

    @Test
    @DisplayName("Should map Category entity to CategoryResponseDto successfully")
    void givenCategory_whenToResponse_thenReturnsCorrectDto() {
        final Category category = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_ELECTRONICS")
                .build();

        final Optional<CategoryResponseDto> optionalDto = categoryMapper.toResponse(category);

        assertThat(optionalDto).isPresent();
        final CategoryResponseDto dto = optionalDto.get();
        assertThat(dto.getId()).isEqualTo(category.getId());
        assertThat(dto.getName()).isEqualTo(category.getName());
        assertThat(dto.getDescription()).isEqualTo(category.getDescription());
        assertThat(dto.getCategoryType()).isEqualTo(category.getCategoryType());
        assertThat(dto.getCategoryGroupType()).isEqualTo(category.getCategoryGroupType());
    }

    @Test
    @DisplayName("Should return empty Optional when mapping null Category to CategoryResponseDto")
    void givenNullCategory_whenToResponse_thenReturnsEmptyOptional() {
        final Optional<CategoryResponseDto> optionalDto = categoryMapper.toResponse(null);
        assertThat(optionalDto).isEmpty();
    }

    @Test
    @DisplayName("Should map CategoryRequestDto to Category entity successfully")
    void givenCategoryRequestDto_whenToEntity_thenReturnsCorrectCategory() {
        final CategoryRequestDto dto = CategoryRequestDto.builder()
                .name("Books")
                .description("Literary works")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_CATEGORY_GOODS")
                .build();

        final Optional<Category> optionalCategory = categoryMapper.toEntity(dto);

        assertThat(optionalCategory).isPresent();
        final Category category = optionalCategory.get();
        assertThat(category.getName()).isEqualTo(dto.getName());
        assertThat(category.getDescription()).isEqualTo(dto.getDescription());
        assertThat(category.getCategoryType()).isEqualTo(dto.getCategoryType());
        assertThat(category.getCategoryGroupType()).isEqualTo(dto.getCategoryGroupType());
        assertThat(category.getId()).isNull();
    }

    @Test
    @DisplayName("Should return empty Optional when mapping null CategoryRequestDto to Category entity")
    void givenNullCategoryRequestDto_whenToEntity_thenReturnsEmptyOptional() {
        final Optional<Category> optionalCategory = categoryMapper.toEntity(null);
        assertThat(optionalCategory).isEmpty();
    }

    @Test
    @DisplayName("Should map Category with null description to DTO with null description")
    void givenCategoryWithNullDescription_whenToResponse_thenDescriptionIsNullInDto() {
        final Category category = Category.builder()
                .id(2L)
                .name("Food")
                .description(null)
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("GOODS")
                .build();

        final Optional<CategoryResponseDto> optionalDto = categoryMapper.toResponse(category);

        assertThat(optionalDto).isPresent();
        final CategoryResponseDto dto = optionalDto.get();
        assertThat(dto.getDescription()).isNull();
    }

    @Test
    @DisplayName("Should map CategoryRequestDto with null description to Entity with null description")
    void givenCategoryRequestDtoWithNullDescription_whenToEntity_thenDescriptionIsNullInEntity() {
        final CategoryRequestDto dto = CategoryRequestDto.builder()
                .name("Services")
                .description(null)
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("SERVICES")
                .build();

        final Optional<Category> optionalCategory = categoryMapper.toEntity(dto);

        assertThat(optionalCategory).isPresent();
        final Category category = optionalCategory.get();
        assertThat(category.getDescription()).isNull();
    }
}