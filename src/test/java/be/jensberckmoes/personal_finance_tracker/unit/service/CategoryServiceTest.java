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
import static org.junit.jupiter.api.Assertions.*;
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
    @DisplayName("Should successfully create a new category with valid details")
    void givenValidCategory_whenCreate_thenCategorySuccessfullyCreated() {
        final Category categoryToCreate = Category.builder()
                .name("Groceries")
                .description("Daily food and household items")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_FOOD")
                .build();
        final Category savedCategoryMock = Category.builder()
                .id(1L)
                .name(categoryToCreate.getName())
                .description(categoryToCreate.getDescription())
                .categoryType(categoryToCreate.getCategoryType())
                .categoryGroupType(categoryToCreate.getCategoryGroupType())
                .build();
        final CategoryRequestDto categoryRequestDto = CategoryRequestDto.builder()
                .name(categoryToCreate.getName())
                .description(categoryToCreate.getDescription())
                .categoryType(categoryToCreate.getCategoryType())
                .categoryGroupType(categoryToCreate.getCategoryGroupType())
                .build();

        when(categoryMapper.toEntity(any(CategoryRequestDto.class))).thenReturn(Optional.of(categoryToCreate));
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategoryMock);
        when(categoryMapper.toResponse(any(Category.class)))
                .thenReturn(Optional.of(CategoryResponseDto.builder()
                        .id(savedCategoryMock.getId())
                        .name(savedCategoryMock.getName())
                        .description(savedCategoryMock.getDescription())
                        .categoryType(savedCategoryMock.getCategoryType())
                        .categoryGroupType(savedCategoryMock.getCategoryGroupType())
                        .build()));

        final CategoryResponseDto createdCategoryResponse = categoryService.createCategory(categoryRequestDto);

        assertThat(createdCategoryResponse).isNotNull();
        assertThat(createdCategoryResponse.getId()).isNotNull();
        assertAll(
                () -> assertEquals("Groceries", createdCategoryResponse.getName(), "Name should match"),
                () -> assertEquals("Daily food and household items", createdCategoryResponse.getDescription(), "Description should match"),
                () -> assertEquals(CategoryType.EXPENSE, createdCategoryResponse.getCategoryType(), "CategoryType should match"),
                () -> assertEquals("EXPENSE_FOOD", createdCategoryResponse.getCategoryGroupType(), "CategoryGroupType should match")
        );

        verify(categoryMapper, times(1)).toEntity(categoryRequestDto);
        verify(categoryRepository, times(1)).save(categoryToCreate);
        verify(categoryMapper, times(1)).toResponse(savedCategoryMock);
    }

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

        when(categoryMapper.toEntity(newCategoryRequestDto)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByName(duplicateCategoryName)).thenReturn(Optional.of(existingCategory));
        assertThrows(DuplicateCategoryNameException.class, () ->
                categoryService.createCategory(newCategoryRequestDto)
        );
        verify(categoryMapper, times(1)).toEntity(newCategoryRequestDto);
        verify(categoryRepository, times(1)).findByName(duplicateCategoryName);
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryMapper, never()).toResponse(any(Category.class));
    }

}