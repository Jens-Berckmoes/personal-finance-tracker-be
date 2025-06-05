package be.jensberckmoes.personal_finance_tracker.service.impl;

import be.jensberckmoes.personal_finance_tracker.dto.CategoryRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.CategoryResponseDto;
import be.jensberckmoes.personal_finance_tracker.exception.InvalidInputException;
import be.jensberckmoes.personal_finance_tracker.model.CategoryMapper;
import be.jensberckmoes.personal_finance_tracker.model.entity.Category;
import be.jensberckmoes.personal_finance_tracker.repository.CategoryRepository;
import be.jensberckmoes.personal_finance_tracker.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public CategoryResponseDto createCategory(final CategoryRequestDto categoryRequestDto) {
        final Category fromRequest = categoryMapper.toEntity(categoryRequestDto).orElseThrow(() -> new InvalidInputException("Invalid CategoryRequestDto received (object is null)."));
        final Category saved = categoryRepository.save(fromRequest);
        return categoryMapper.toResponse(saved).orElseThrow(() -> new InvalidInputException("Invalid Category entity received from database. (object is null)."));
    }
}
