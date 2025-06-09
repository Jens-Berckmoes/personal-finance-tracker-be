package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.dto.CategoryRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.CategoryResponseDto;
import be.jensberckmoes.personal_finance_tracker.exception.DuplicateCategoryNameException;
import be.jensberckmoes.personal_finance_tracker.exception.InvalidInputException;
import be.jensberckmoes.personal_finance_tracker.model.CategoryMapper;
import be.jensberckmoes.personal_finance_tracker.model.entity.Category;
import be.jensberckmoes.personal_finance_tracker.model.enums.CategoryType;
import be.jensberckmoes.personal_finance_tracker.repository.CategoryRepository;
import be.jensberckmoes.personal_finance_tracker.service.impl.CategoryServiceImpl;
import jakarta.persistence.EntityNotFoundException;
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

        when(categoryRepository.findByNameIgnoreCase(duplicateCategoryName)).thenReturn(Optional.of(existingCategory));
        assertThrows(DuplicateCategoryNameException.class, () ->
                categoryService.createCategory(newCategoryRequestDto)
        );

        verify(categoryRepository, times(1)).findByNameIgnoreCase(duplicateCategoryName);
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
        when(categoryRepository.findByNameIgnoreCase(uniqueCategoryName)).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        doReturn(Optional.of(expectedResponseDto))
                .when(categoryMapper).toResponse(savedCategory);

        final CategoryResponseDto actualResponseDto = categoryService.createCategory(newCategoryRequestDto);

        assertThat(actualResponseDto).isNotNull();
        assertThat(actualResponseDto.getId()).isEqualTo(expectedResponseDto.getId());
        assertThat(actualResponseDto.getName()).isEqualTo(expectedResponseDto.getName());

        verify(categoryMapper, times(1)).toEntity(newCategoryRequestDto);
        verify(categoryRepository, times(1)).findByNameIgnoreCase(uniqueCategoryName);
        verify(categoryRepository, times(1)).save(categoryToSave);
        verify(categoryMapper, times(1)).toResponse(savedCategory);
    }

    @Test
    @DisplayName("Should throw InvalidInputException when CategoryRequestDto is null")
    void givenNullCategoryRequestDto_whenCreateCategory_thenThrowsInvalidInputException() {
        final InvalidInputException thrown = assertThrows(InvalidInputException.class, () ->
                categoryService.createCategory(null)
        );

        assertThat(thrown.getMessage()).isEqualTo("CategoryRequestDto cannot be null.");

        verify(categoryRepository, never()).findByNameIgnoreCase(any(String.class));
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryMapper, never()).toResponse(any(Category.class));
    }

    @Test
    @DisplayName("Should successfully update category when ID exists and name is unique or unchanged")
    void givenExistingIdAndValidCategoryRequestDto_whenUpdateCategory_thenReturnsUpdatedCategoryResponseDto() {

        final Long categoryId = 1L;
        final String oldName = "Old Name";
        final String newName = "New Name";

        final Category existingCategory = Category.builder()
                .id(categoryId)
                .name(oldName)
                .description("Original description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("ORIGINAL_GROUP")
                .build();

        final CategoryRequestDto updateDto = CategoryRequestDto.builder()
                .name(newName)
                .description("Updated description")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("UPDATED_GROUP")
                .build();

        final Category categoryToSave = Category.builder()
                .name(newName)
                .description("Updated description")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("UPDATED_GROUP")
                .build();
        categoryToSave.setId(categoryId);

        final Category savedCategory = Category.builder()
                .id(categoryId)
                .name(newName)
                .description("Updated description")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("UPDATED_GROUP")
                .build();

        final CategoryResponseDto expectedResponseDto = CategoryResponseDto.builder()
                .id(categoryId)
                .name(newName)
                .description("Updated description")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("UPDATED_GROUP")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByNameIgnoreCase(newName)).thenReturn(Optional.empty());
        doReturn(Optional.of(categoryToSave))
                .when(categoryMapper).toEntity(updateDto);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        doReturn(Optional.of(expectedResponseDto))
                .when(categoryMapper).toResponse(savedCategory);

        final CategoryResponseDto actualResponseDto = categoryService.updateCategory(categoryId, updateDto);

        assertThat(actualResponseDto).isNotNull();
        assertThat(actualResponseDto.getId()).isEqualTo(categoryId);
        assertThat(actualResponseDto.getName()).isEqualTo(newName);
        assertThat(actualResponseDto.getDescription()).isEqualTo(updateDto.getDescription());
        assertThat(actualResponseDto.getCategoryType()).isEqualTo(updateDto.getCategoryType());
        assertThat(actualResponseDto.getCategoryGroupType()).isEqualTo(updateDto.getCategoryGroupType());

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).findByNameIgnoreCase(newName);
        verify(categoryMapper, times(1)).toEntity(updateDto);
        verify(categoryRepository, times(1)).save(categoryToSave);
        verify(categoryMapper, times(1)).toResponse(savedCategory);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when category ID does not exist")
    void givenNonExistingId_whenUpdateCategory_thenThrowsEntityNotFoundException() {
        final Long nonExistingId = 99L;
        final CategoryRequestDto updateDto = CategoryRequestDto.builder()
                .name("Some Name")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("SOME_GROUP")
                .build();

        when(categoryRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        final EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                categoryService.updateCategory(nonExistingId, updateDto)
        );

        assertThat(thrown.getMessage()).isEqualTo("Category not found with id: " + nonExistingId);

        verify(categoryRepository, times(1)).findById(nonExistingId);
        verify(categoryRepository, never()).findByNameIgnoreCase(any(String.class));
        verify(categoryMapper, never()).toEntity(any(CategoryRequestDto.class));
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryMapper, never()).toResponse(any(Category.class));
    }

    @Test
    @DisplayName("Should throw DuplicateCategoryNameException when new name conflicts with another category")
    void givenExistingCategoryAndDuplicateNameForOtherCategory_whenUpdateCategory_thenThrowsDuplicateCategoryNameException() {
        final Long categoryToUpdateId = 1L;
        final Long conflictingCategoryId = 2L;
        final String oldName = "Original Category Name";
        final String duplicateName = "Existing Name for Another Category";

        final Category existingCategoryToUpdate = Category.builder()
                .id(categoryToUpdateId)
                .name(oldName)
                .description("Description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("GROUP")
                .build();

        final Category conflictingCategory = Category.builder()
                .id(conflictingCategoryId)
                .name(duplicateName)
                .description("Conflicting description")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("ANOTHER_GROUP")
                .build();

        final CategoryRequestDto updateDto = CategoryRequestDto.builder()
                .name(duplicateName)
                .description("New description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("GROUP")
                .build();


        when(categoryRepository.findById(categoryToUpdateId)).thenReturn(Optional.of(existingCategoryToUpdate));

        when(categoryRepository.findByNameIgnoreCase(duplicateName)).thenReturn(Optional.of(conflictingCategory));

        final DuplicateCategoryNameException thrown = assertThrows(DuplicateCategoryNameException.class, () ->
                categoryService.updateCategory(categoryToUpdateId, updateDto)
        );

        assertThat(thrown.getMessage()).isEqualTo("Another category with name '" + duplicateName + "' already exists.");

        verify(categoryRepository, times(1)).findById(categoryToUpdateId);
        verify(categoryRepository, times(1)).findByNameIgnoreCase(duplicateName);
        verify(categoryMapper, never()).toEntity(any(CategoryRequestDto.class));
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryMapper, never()).toResponse(any(Category.class));
    }

    @Test
    @DisplayName("Should successfully update category when new name is identical to existing category's own name (no conflict)")
    void givenExistingCategoryAndSameNameAsOwn_whenUpdateCategory_thenReturnsUpdatedCategoryResponseDto() {
        final Long categoryId = 1L;
        final String sameName = "My Category Name";

        final Category existingCategory = Category.builder()
                .id(categoryId)
                .name(sameName)
                .description("Original description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("ORIGINAL_GROUP")
                .build();

        final CategoryRequestDto updateDto = CategoryRequestDto.builder()
                .name(sameName)
                .description("Updated description")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("UPDATED_GROUP")
                .build();

        final Category categoryToSave = Category.builder()
                .name(sameName)
                .description("Updated description")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("UPDATED_GROUP")
                .build();
        categoryToSave.setId(categoryId);

        final Category savedCategory = Category.builder()
                .id(categoryId)
                .name(sameName)
                .description("Updated description")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("UPDATED_GROUP")
                .build();

        final CategoryResponseDto expectedResponseDto = CategoryResponseDto.builder()
                .id(categoryId)
                .name(sameName)
                .description("Updated description")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("UPDATED_GROUP")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        doReturn(Optional.of(categoryToSave))
                .when(categoryMapper).toEntity(updateDto);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        doReturn(Optional.of(expectedResponseDto))
                .when(categoryMapper).toResponse(savedCategory);

        final CategoryResponseDto actualResponseDto = categoryService.updateCategory(categoryId, updateDto);

        assertThat(actualResponseDto).isNotNull();
        assertThat(actualResponseDto.getName()).isEqualTo(sameName);

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryMapper, times(1)).toEntity(updateDto);
        verify(categoryRepository, times(1)).save(categoryToSave);
        verify(categoryMapper, times(1)).toResponse(savedCategory);
    }

    @Test
    @DisplayName("Should throw InvalidInputException when CategoryRequestDto is null for update")
    void givenNullCategoryRequestDto_whenUpdateCategory_thenThrowsInvalidInputException() {
        final Long categoryId = 1L;
        final CategoryRequestDto nullCategoryRequestDto = null;

        InvalidInputException thrown = assertThrows(InvalidInputException.class, () ->
                categoryService.updateCategory(categoryId, nullCategoryRequestDto)
        );

        assertThat(thrown.getMessage()).isEqualTo("CategoryRequestDto cannot be null for update operation.");

        verify(categoryRepository, never()).findById(any(Long.class));
        verify(categoryRepository, never()).findByNameIgnoreCase(any(String.class));
        verify(categoryMapper, never()).toEntity(any(CategoryRequestDto.class));
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryMapper, never()).toResponse(any(Category.class));
    }

}