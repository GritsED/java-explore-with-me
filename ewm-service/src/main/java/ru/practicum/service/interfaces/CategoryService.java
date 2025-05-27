package ru.practicum.service.interfaces;

import ru.practicum.dto.request.NewCategoryDto;
import ru.practicum.dto.response.CategoryDto;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto newCategory);

    void deleteCategory(Long id);

    CategoryDto updateCategory(CategoryDto categoryDto, Long id);
}
