package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.dto.CategoryRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.CategoryResponseDto;
import jakarta.validation.Valid;

public interface CategoryService {
    CategoryResponseDto createCategory(@Valid final CategoryRequestDto categoryRequestDto);
}
