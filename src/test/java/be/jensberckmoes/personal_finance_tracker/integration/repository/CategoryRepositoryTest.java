package be.jensberckmoes.personal_finance_tracker.integration.repository;

import be.jensberckmoes.personal_finance_tracker.model.entity.Category;
import be.jensberckmoes.personal_finance_tracker.model.enums.CategoryType;
import be.jensberckmoes.personal_finance_tracker.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CategoryRepositoryTest {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    public void givenValidCategory_whenFindById_findThatCategory() {
        final Category categoryToSave = Category.builder()
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_HOUSING")
                .name("HOUSING")
                .description("Rent")
                .build();

        final Category savedCategory = categoryRepository.save(categoryToSave);
        final Optional<Category> possibleFoundCategory = categoryRepository.findById(savedCategory.getId());

        assertTrue(possibleFoundCategory.isPresent());
        final Category foundCategory = possibleFoundCategory.get();

        assertAll(
                () -> assertEquals("HOUSING", foundCategory.getName()),
                () -> assertEquals("Rent", foundCategory.getDescription()),
                () -> assertNotNull(foundCategory.getId()),
                () -> assertEquals(savedCategory.getId(), foundCategory.getId()),
                () -> assertEquals(CategoryType.EXPENSE, foundCategory.getCategoryType()),
                () -> assertEquals("EXPENSE_HOUSING", foundCategory.getCategoryGroupType())
        );
    }

    @Test()
    public void givenTwoCategoriesWithSameName_whenAttemptingToSaveSecond_thenThrowsDataIntegrityViolationException() {
        final Category categoryToSave = Category.builder()
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_HOUSING")
                .name("HOUSING")
                .description("Rent")
                .build();
        categoryRepository.save(categoryToSave);
        final Category secondCategoryToSave = Category.builder()
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_HOUSING")
                .name("HOUSING")
                .description("Rent")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> categoryRepository.save(secondCategoryToSave));
    }

    @ParameterizedTest(name = "{index}: {1}")
    @MethodSource("invalidCategoryNames")
    @DisplayName("Should throw exception when saving category with invalid name property")
    void givenCategoryWithInvalidNameProperty_whenSave_thenThrowsException(String invalidName, String testDescription) {

        final Category category = Category.builder()
                .name(invalidName)
                .description("Test Description for " + testDescription)
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("SOME_CATEGORY_GROUP_TYPE")
                .build();

        assertThrows(ConstraintViolationException.class, () -> categoryRepository.save(category));
    }

    private static Stream<Arguments> invalidCategoryNames() {
        return Stream.of(
                Arguments.of(" ", "blank name"),
                Arguments.of("a".repeat(51), "name exceeding 50 characters"),
                Arguments.of(null, "null name")
        );
    }

    @Test
    public void givenCategoryWithLongDescription_whenSave_thenThrowsException() {
        final Category category = Category.builder()
                .name("TEST")
                .description("a".repeat(256))
                .build();
        assertThrows(ConstraintViolationException.class, () -> categoryRepository.save(category));
    }

    @ParameterizedTest(name = "{index}: {1}")
    @MethodSource("invalidCategoryGroupTypes")
    @DisplayName("Should throw exception when saving category with invalid categoryGroupType property")
    void givenCategoryWithInvalidCategoryGroupTypeProperty_whenSave_thenThrowsException(final String invalidCategoryGroupType, final String testDescription) {

        final Category category = Category.builder()
                .name("TEST")
                .description("Test Description for " + testDescription)
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType(invalidCategoryGroupType)
                .build();

        assertThrows(ConstraintViolationException.class, () -> categoryRepository.save(category));
    }

    private static Stream<Arguments> invalidCategoryGroupTypes() {
        return Stream.of(
                Arguments.of(" ", "blank group type"),
                Arguments.of("a".repeat(101), "group type exceeding 100 characters"),
                Arguments.of(null, "null group type")
        );
    }

    @Test
    public void givenCategoryWithOnlyName_whenSave_thenVerifySavedSuccessfullyAndOtherFieldsNullOrDefault() {
        final Category category = Category.builder()
                .name("TEST")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_TEST")
                .build();
        final Category savedCategory = categoryRepository.save(category);
        final Optional<Category> possibleFoundCategory = categoryRepository.findById(savedCategory.getId());

        assertTrue(possibleFoundCategory.isPresent());
        final Category foundCategory = possibleFoundCategory.get();

        assertAll(
                () -> assertEquals("TEST", foundCategory.getName()),
                () -> assertNull(foundCategory.getDescription()),
                () -> assertNotNull(foundCategory.getId()),
                () -> assertEquals(savedCategory.getId(), foundCategory.getId()),
                () -> assertEquals(CategoryType.EXPENSE, foundCategory.getCategoryType()),
                () -> assertEquals("EXPENSE_TEST", foundCategory.getCategoryGroupType())
        );
    }

    @Test
    public void givenNonExistingCategory_whenFindById_returnsEmptyOptional() {
        long nonExistentId = 99L;

        final Optional<Category> possibleNonFoundCategory = categoryRepository.findById(nonExistentId);

        assertTrue(possibleNonFoundCategory.isEmpty(), "Optional should be empty for a non-existent ID");
    }

    @Test
    public void GivenEmptyDatabase_whenFindAll_thenEmptyListIsReturned() {
        categoryRepository.deleteAll();

        final List<Category> categories = categoryRepository.findAll();

        assertNotNull(categories);
        assertThat(categories).hasSize(0);
    }

    @Test
    public void givenSeveralSavedCategories_whenFindAll_thenContainsAllSavedCategories() {
        categoryRepository.saveAll(
                List.of(
                        Category.builder()
                                .name("HOUSING")
                                .description("Rent")
                                .categoryType(CategoryType.EXPENSE)
                                .categoryGroupType("EXPENSE_GROUP_TYPE")
                                .build(),
                        Category.builder()
                                .name("LOAN")
                                .description("loan")
                                .categoryType(CategoryType.EXPENSE)
                                .categoryGroupType("EXPENSE_GROUP_TYPE")
                                .build(),
                        Category.builder()
                                .name("WAGE")
                                .description("wage")
                                .categoryType(CategoryType.INCOME)
                                .categoryGroupType("INCOME_GROUP_TYPE")
                                .build()
                )
        );
        final List<Category> categories = categoryRepository.findAll();

        assertNotNull(categories);
        assertThat(categories).hasSize(3);
        assertTrue(categories.stream().anyMatch(c -> c.getName().equals("HOUSING")));
        assertTrue(categories.stream().anyMatch(c -> c.getName().equals("LOAN")));
        assertTrue(categories.stream().anyMatch(c -> c.getName().equals("WAGE")));
    }

    @Test
    public void givenSavedCategory_whenFindByName_thenIsFoundCorrectly() {
        final Category category = Category.builder()
                .name("TEST")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_TEST")
                .build();
        final Category savedCategory = categoryRepository.save(category);
        final Optional<Category> possibleFoundCategory = categoryRepository.findByNameIgnoreCase(savedCategory.getName());

        assertTrue(possibleFoundCategory.isPresent());
        final Category foundCategory = possibleFoundCategory.get();

        assertAll(
                () -> assertEquals("TEST", foundCategory.getName()),
                () -> assertNull(foundCategory.getDescription()),
                () -> assertNotNull(foundCategory.getId()),
                () -> assertEquals(savedCategory.getId(), foundCategory.getId()),
                () -> assertEquals(CategoryType.EXPENSE, foundCategory.getCategoryType()),
                () -> assertEquals("EXPENSE_TEST", foundCategory.getCategoryGroupType())
        );
    }

    @Test
    public void givenWrongCategoryName_whenFindByName_thenIsEmpty() {
        final Category category = Category.builder()
                .name("TEST")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_TEST")
                .build();
        final Category savedCategory = categoryRepository.save(category);
        assertEquals("TEST", savedCategory.getName());
        assertThat(categoryRepository.findAll()).hasSize(1);
        final Optional<Category> possibleFoundCategory = categoryRepository.findByNameIgnoreCase("-");

        assertTrue(possibleFoundCategory.isEmpty());
    }

    @Test
    public void givenSavedCategory_whenModifyNameAndSave_thenIsUpdatedCorrectly() {
        final Category categoryToSave = Category.builder()
                .name("HOUSING")
                .description("Rent")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_GROUP_TYPE")
                .build();

        final Category savedCategory = categoryRepository.save(categoryToSave);
        savedCategory.setName("RENT");
        final Category rentCategory = categoryRepository.save(savedCategory);
        final Optional<Category> possibleFoundCategory = categoryRepository.findById(savedCategory.getId());

        assertTrue(possibleFoundCategory.isPresent());
        assertEquals(rentCategory.getName(), possibleFoundCategory.get().getName());
    }

    @Test
    public void givenSavedCategory_whenModifyDescriptionAndSave_thenIsUpdatedCorrectly() {
        final Category categoryToSave = Category.builder()
                .name("HOUSING")
                .description("Rent")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_GROUP_TYPE")
                .build();

        final Category savedCategory = categoryRepository.save(categoryToSave);
        savedCategory.setDescription("housing");
        final Category rentCategory = categoryRepository.save(savedCategory);
        final Optional<Category> possibleFoundCategory = categoryRepository.findById(savedCategory.getId());

        assertTrue(possibleFoundCategory.isPresent());
        assertEquals(rentCategory.getDescription(), possibleFoundCategory.get().getDescription());
    }

    @Test
    public void givenSavedCategory_whenModifyCategoryTypeAndSave_thenIsUpdatedCorrectly() {
        final Category categoryToSave = Category.builder()
                .name("HOUSING")
                .description("Rent")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_GROUP_TYPE")
                .build();

        final Category savedCategory = categoryRepository.save(categoryToSave);
        savedCategory.setCategoryType(CategoryType.INCOME);
        final Category rentCategory = categoryRepository.save(savedCategory);
        final Optional<Category> possibleFoundCategory = categoryRepository.findById(savedCategory.getId());

        assertTrue(possibleFoundCategory.isPresent());
        assertEquals(rentCategory.getCategoryType(), possibleFoundCategory.get().getCategoryType());
    }

    @Test
    public void givenSavedCategory_whenModifyCategoryGroupAndSave_thenIsUpdatedCorrectly() {
        final Category categoryToSave = Category.builder()
                .name("HOUSING")
                .description("Rent")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_GROUP_TYPE")
                .build();

        final Category savedCategory = categoryRepository.save(categoryToSave);
        savedCategory.setCategoryGroupType("EXPENSE_GROUP_TYPE2");
        final Category rentCategory = categoryRepository.save(savedCategory);
        final Optional<Category> possibleFoundCategory = categoryRepository.findById(savedCategory.getId());

        assertTrue(possibleFoundCategory.isPresent());
        assertEquals(rentCategory.getCategoryGroupType(), possibleFoundCategory.get().getCategoryGroupType());
    }

    @Test
    public void givenSavedCategory_whenDeleteCategory_thenEmptyOptional() {
        final Category cat1 = Category.builder()
                .name("HOUSING")
                .description("Rent")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_GROUP_TYPE")
                .build();
        final Category cat2 = Category.builder()
                .name("LOAN")
                .description("loan")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_GROUP_TYPE")
                .build();
        final Category cat3 = Category.builder()
                .name("WAGE")
                .description("wage")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("INCOME_GROUP_TYPE")
                .build();
        final List<Category> categoryList = categoryRepository.saveAll(List.of(cat1, cat2, cat3));
        final List<Category> categories = categoryRepository.findAll();
        assertNotNull(categories);
        assertThat(categoryRepository.findAll()).hasSize(3);
        assertTrue(categoryRepository.findById(categoryList.getFirst().getId()).isPresent());
        assertTrue(categoryRepository.findById(categoryList.get(1).getId()).isPresent());
        assertTrue(categoryRepository.findById(categoryList.getLast().getId()).isPresent());

        categoryRepository.deleteById(cat1.getId());

        final List<Category> newlyRequestedCategories = categoryRepository.findAll();
        assertNotNull(newlyRequestedCategories);
        assertThat(categoryRepository.findAll()).hasSize(2);
        assertTrue(categoryRepository.findById(cat1.getId()).isEmpty());
    }

    @Test
    public void givenNoCategoryExisting_whenDeleteById_thenNoException() {
        assertDoesNotThrow(() -> categoryRepository.deleteById(1L));
    }

    @Test
    public void givenSavedCategories_whenDeleteAll_thenEmptyList() {
        final Category cat1 = Category.builder()
                .name("HOUSING")
                .description("Rent")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_GROUP_TYPE")
                .build();
        final Category cat2 = Category.builder()
                .name("LOAN")
                .description("loan")
                .categoryType(CategoryType.EXPENSE)
                .categoryGroupType("EXPENSE_GROUP_TYPE")
                .build();
        final Category cat3 = Category.builder()
                .name("WAGE")
                .description("wage")
                .categoryType(CategoryType.INCOME)
                .categoryGroupType("INCOME_GROUP_TYPE")
                .build();
        final List<Category> categoryList = categoryRepository.saveAll(List.of(cat1, cat2, cat3));
        final List<Category> categories = categoryRepository.findAll();
        assertNotNull(categories);
        assertThat(categories).hasSize(3);
        assertTrue(categoryRepository.findById(categoryList.getFirst().getId()).isPresent());
        assertTrue(categoryRepository.findById(categoryList.get(1).getId()).isPresent());
        assertTrue(categoryRepository.findById(categoryList.getLast().getId()).isPresent());

        categoryRepository.deleteAll();

        final List<Category> newlyRequestedCategories = categoryRepository.findAll();
        assertNotNull(newlyRequestedCategories);
        assertThat(newlyRequestedCategories).hasSize(0);
    }
}