package be.jensberckmoes.personal_finance_tracker.service.impl;

import be.jensberckmoes.personal_finance_tracker.dto.CategoryRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.CategoryResponseDto;
import be.jensberckmoes.personal_finance_tracker.exception.DuplicateCategoryNameException;
import be.jensberckmoes.personal_finance_tracker.exception.InvalidInputException;
import be.jensberckmoes.personal_finance_tracker.model.CategoryMapper;
import be.jensberckmoes.personal_finance_tracker.model.entity.Category;
import be.jensberckmoes.personal_finance_tracker.repository.CategoryRepository;
import be.jensberckmoes.personal_finance_tracker.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponseDto createCategory(@Valid final CategoryRequestDto categoryRequestDto) {
        if (Objects.isNull(categoryRequestDto)) {
            throw new InvalidInputException("CategoryRequestDto cannot be null.");
        }
        if (categoryRepository.findByName(categoryRequestDto.getName()).isPresent()) {
            throw new DuplicateCategoryNameException("Category with name '" + categoryRequestDto.getName() + "' already exists.");
        }
        final Category fromRequest = categoryMapper.toEntity(categoryRequestDto).orElseThrow(() -> new InvalidInputException("Invalid categoryRequestDto received by mapper."));
        final Category saved = categoryRepository.save(fromRequest);
        return categoryMapper.toResponse(saved).orElseThrow(() -> new InvalidInputException("Invalid Category entity received from database. (object is null)."));
    }
}
