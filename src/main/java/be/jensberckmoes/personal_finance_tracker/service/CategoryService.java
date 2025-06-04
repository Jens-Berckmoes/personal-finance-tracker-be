package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.dto.CategoryRequestDto;
import be.jensberckmoes.personal_finance_tracker.dto.CategoryResponseDto;

public interface CategoryService {
    CategoryResponseDto createCategory(final CategoryRequestDto categoryRequestDto);
}
