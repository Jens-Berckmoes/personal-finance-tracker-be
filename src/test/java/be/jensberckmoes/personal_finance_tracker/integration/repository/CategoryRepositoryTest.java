package be.jensberckmoes.personal_finance_tracker.integration.repository;

import be.jensberckmoes.personal_finance_tracker.model.Category;
import be.jensberckmoes.personal_finance_tracker.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
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
}