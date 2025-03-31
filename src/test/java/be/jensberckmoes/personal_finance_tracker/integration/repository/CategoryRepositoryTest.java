package be.jensberckmoes.personal_finance_tracker.integration.repository;

import be.jensberckmoes.personal_finance_tracker.model.Category;
import be.jensberckmoes.personal_finance_tracker.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CategoryRepositoryTest {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private EntityManager entityManager;
    @MockitoBean
    private SecurityFilterChain mockSecurityFilterChain;

    @Test
    public void givenValidCategory_whenFindById_findThatCategory() {
        final Category categoryToSave = Category.builder()
                .name("HOUSING")
                .description("Rent")
                .build();

        final Category savedCategory = categoryRepository.save(categoryToSave);
        final Optional<Category> possibleFoundCategory = categoryRepository.findById(savedCategory.getId());

        assertNotNull(possibleFoundCategory);
        assertTrue(possibleFoundCategory.isPresent());
        final Category foundCategory = possibleFoundCategory.get();
        assertAll(
                () -> assertEquals("HOUSING", foundCategory.getName()),
                () -> assertEquals("Rent", foundCategory.getDescription()),
                () -> assertEquals(1L, foundCategory.getId())
        );
    }

    @Test
    public void givenNonExistingCategory_whenFindById_returnsEmptyOptional() {
        final Category categoryToSave = Category.builder()
                .name("HOUSING")
                .description("Rent")
                .build();

        final Category savedCategory = categoryRepository.save(categoryToSave);
        final Optional<Category> possibleFoundCategory = categoryRepository.findById(savedCategory.getId());
        final Optional<Category> possibleNonFoundCategory = categoryRepository.findById(2L);

        assertTrue(possibleFoundCategory.isPresent());
        assertTrue(possibleNonFoundCategory.isEmpty());
    }

    @Test
    public void givenCategoryWithBlankName_whenSave_thenThrowsException() {
        final Category category = Category.builder()
                .name(" ")
                .description("Test")
                .build();
        assertThrows(ConstraintViolationException.class, () -> categoryRepository.save(category));
    }

    @Test
    public void givenCategoryWithLongName_whenSave_thenThrowsException() {
        final Category category = Category.builder()
                .name("a".repeat(51))
                .description("Test")
                .build();
        assertThrows(ConstraintViolationException.class, () -> categoryRepository.save(category));
    }

    @Test
    public void givenCategoryWithNullName_whenSave_thenThrowsException() {
        final Category category = Category.builder()
                .name(null)
                .description("Test")
                .build();
        assertThrows(ConstraintViolationException.class, () -> categoryRepository.save(category));
    }

    @Test
    public void givenDuplicateCategoryName_whenSave_thenThrowsException() {
        final Category category = Category.builder()
                .name("Duplicate")
                .description("Test")
                .build();
        categoryRepository.save(category);
        final Category duplicateCategory = Category.builder()
                .name("Duplicate")
                .description("Test2")
                .build();
        assertThrows(DataIntegrityViolationException.class, () -> categoryRepository.save(duplicateCategory));
    }
}