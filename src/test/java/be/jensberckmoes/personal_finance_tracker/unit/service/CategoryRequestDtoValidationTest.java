package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.model.entity.Category;
import be.jensberckmoes.personal_finance_tracker.model.enums.CategoryType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CategoryRequestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (final ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Category: Should have violation when name is null")
    void givenCategoryWithNullName_thenShouldDetectViolation() {
        final Category category = Category.builder()
                .id(1L)
                .name(null)
                .description("Some description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_FOOD")
                .build();

        final Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessageTemplate().contains("Category name cannot be blank"));
    }

    @Test
    @DisplayName("Category: Should have violation when name is empty")
    void givenCategoryWithEmptyName_thenShouldDetectViolation() {
        final Category category = Category.builder()
                .id(1L)
                .name("")
                .description("Some description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_FOOD")
                .build();

        final Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessageTemplate().contains("Category name cannot be blank"));
    }

    @Test
    @DisplayName("Category: Should have violation when name is only whitespace")
    void givenCategoryWithWhitespaceName_thenShouldDetectViolation() {
        final Category category = Category.builder()
                .id(1L)
                .name("   ")
                .description("Some description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_FOOD")
                .build();

        final Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessageTemplate().contains("Category name cannot be blank"));
    }

    @Test
    @DisplayName("Category: Should have violation when name exceeds 50 characters")
    void givenCategoryWithLongName_thenShouldDetectViolation() {
        final Category category = Category.builder()
                .id(1L)
                .name("a".repeat(51))
                .description("Some description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_FOOD")
                .build();

        final Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "name".equals(v.getPropertyPath().toString()) && v.getMessage().contains("cannot exceed 50 characters"));
    }

    @Test
    @DisplayName("Category: Should not have violation when name is valid")
    void givenCategoryWithValidName_thenShouldNotDetectViolation() {
        final Category category = Category.builder()
                .id(1L)
                .name("Valid Name")
                .description("Some description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_FOOD")
                .build();

        final Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isEmpty();
    }


    @Test
    @DisplayName("Category: Should have violation when description exceeds 255 characters")
    void givenCategoryWithLongDescription_thenShouldDetectViolation() {
        final Category category = Category.builder()
                .id(1L)
                .name("Valid Name")
                .description("a".repeat(256))
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_FOOD")
                .build();

        final Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "description".equals(v.getPropertyPath().toString()) && v.getMessage().contains("cannot exceed 255 characters"));
    }

    @Test
    @DisplayName("Category: Should not have violation when description is null (optional field)")
    void givenCategoryWithNullDescription_thenShouldNotDetectViolation() {
        final Category category = Category.builder()
                .id(1L)
                .name("Valid Name")
                .description(null)
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_FOOD")
                .build();

        final Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isEmpty();
    }


    @Test
    @DisplayName("Category: Should have violation when categoryType is null")
    void givenCategoryWithNullCategoryType_thenShouldDetectViolation() {
        final Category category = Category.builder()
                .id(1L)
                .name("Valid Name")
                .description("Some description")
                .categoryType(null)
                .categoryGroupType("EXPENSE_FOOD")
                .build();

        final Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "categoryType".equals(v.getPropertyPath().toString()) && "must not be null".equals(v.getMessage()));
    }


    @Test
    @DisplayName("Category: Should have violation when categoryGroupType is null")
    void givenCategoryWithNullCategoryGroupType_thenShouldDetectViolation() {
        final Category category = Category.builder()
                .id(1L)
                .name("Valid Name")
                .description("Some description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType(null)
                .build();

        final Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isNotEmpty();

        assertThat(violations).anyMatch(v -> "categoryGroupType".equals(v.getPropertyPath().toString()) && v.getMessage().contains("cannot be blank"));
    }

    @Test
    @DisplayName("Category: Should have violation when categoryGroupType is empty")
    void givenCategoryWithEmptyCategoryGroupType_thenShouldDetectViolation() {
        final Category category = Category.builder()
                .id(1L)
                .name("Valid Name")
                .description("Some description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("")
                .build();

        final Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "categoryGroupType".equals(v.getPropertyPath().toString()) && v.getMessage().contains("cannot be blank"));
    }

    @Test
    @DisplayName("Category: Should have violation when categoryGroupType is only whitespace")
    void givenCategoryWithWhitespaceCategoryGroupType_thenShouldDetectViolation() {
        final Category category = Category.builder()
                .id(1L)
                .name("Valid Name")
                .description("Some description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("   ")
                .build();

        final Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "categoryGroupType".equals(v.getPropertyPath().toString()) && v.getMessage().contains("cannot be blank"));
    }

    @Test
    @DisplayName("Category: Should have violation when categoryGroupType exceeds 100 characters")
    void givenCategoryWithLongCategoryGroupType_thenShouldDetectViolation() {
        final Category category = Category.builder()
                .id(1L)
                .name("Valid Name")
                .description("Some description")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("b".repeat(101))
                .build();

        final Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "categoryGroupType".equals(v.getPropertyPath().toString()) && v.getMessage().contains("cannot exceed 100 characters"));
    }

    @Test
    @DisplayName("Category: Should not have any violations for a completely valid Category")
    void givenValidCategory_thenShouldHaveNoViolations() {
        final Category category = Category.builder()
                .id(1L)
                .name("Household Expenses")
                .description("Monthly household bills and upkeep")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("HOME_MAINTENANCE")
                .build();

        final Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isEmpty();
    }
}