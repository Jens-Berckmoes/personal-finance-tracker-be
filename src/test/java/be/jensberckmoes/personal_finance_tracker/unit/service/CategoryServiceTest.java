package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.dto.CategoryRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.CategoryResponseDto;
import be.jensberckmoes.personal_finance_tracker.exception.DuplicateCategoryNameException;
import be.jensberckmoes.personal_finance_tracker.model.CategoryMapper;
import be.jensberckmoes.personal_finance_tracker.model.entity.Category;
import be.jensberckmoes.personal_finance_tracker.model.enums.CategoryType;
import be.jensberckmoes.personal_finance_tracker.repository.CategoryRepository;
import be.jensberckmoes.personal_finance_tracker.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("Should throw DuplicateCategoryNameException when category name already exists")
    void givenExistingCategoryName_whenCreateCategory_thenThrowsDuplicateCategoryNameException() {
        final String duplicateCategoryName = "Groceries";
        final Category existingCategory = Category.builder()
                .id(1L)
                .name(duplicateCategoryName)
                .description("Existing daily food items")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_FOOD")
                .build();

        final CategoryRequestDto newCategoryRequestDto = CategoryRequestDto.builder()
                .name(duplicateCategoryName)
                .description("New groceries description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_FOOD")
                .build();

        when(categoryRepository.findByName(duplicateCategoryName)).thenReturn(Optional.of(existingCategory));
        assertThrows(DuplicateCategoryNameException.class, () ->
                categoryService.createCategory(newCategoryRequestDto)
        );

        verify(categoryRepository, times(1)).findByName(duplicateCategoryName);
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryMapper, never()).toResponse(any(Category.class));
    }

    @Test
    @DisplayName("Should create category successfully when name does not exist")
    void givenUniqueCategoryName_whenCreateCategory_thenReturnsCreatedCategoryResponseDto() {
        final String uniqueCategoryName = "New Unique Category";
        final Category categoryToSave = Category.builder()
                .name(uniqueCategoryName)
                .description("A brand new category")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("INCOME_WORK")
                .build();
        final Category savedCategory = Category.builder()
                .id(2L)
                .name(uniqueCategoryName)
                .description("A brand new category")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("INCOME_WORK")
                .build();
        final CategoryRequestDto newCategoryRequestDto = CategoryRequestDto.builder()
                .name(uniqueCategoryName)
                .description("A brand new category")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("INCOME_WORK")
                .build();
        final CategoryResponseDto expectedResponseDto = CategoryResponseDto.builder()
                .id(2L)
                .name(uniqueCategoryName)
                .description("A brand new category")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("INCOME_WORK")
                .build();

        doReturn(Optional.of(categoryToSave))
                .when(categoryMapper).toEntity(newCategoryRequestDto);
        when(categoryRepository.findByName(uniqueCategoryName)).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        doReturn(Optional.of(expectedResponseDto))
                .when(categoryMapper).toResponse(savedCategory);

        CategoryResponseDto actualResponseDto = categoryService.createCategory(newCategoryRequestDto);

        assertThat(actualResponseDto).isNotNull();
        assertThat(actualResponseDto.getId()).isEqualTo(expectedResponseDto.getId());
        assertThat(actualResponseDto.getName()).isEqualTo(expectedResponseDto.getName());

        verify(categoryMapper, times(1)).toEntity(newCategoryRequestDto);
        verify(categoryRepository, times(1)).findByName(uniqueCategoryName);
        verify(categoryRepository, times(1)).save(categoryToSave);
        verify(categoryMapper, times(1)).toResponse(savedCategory);
    }

}