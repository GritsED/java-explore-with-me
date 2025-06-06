package ru.practicum.service.interfaces;

import ru.practicum.dto.request.NewCategoryDto;
import ru.practicum.dto.response.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategoryAdmin(NewCategoryDto newCategory);

    void deleteCategoryAdmin(Long id);

    CategoryDto updateCategoryAdmin(CategoryDto categoryDto, Long id);

    List<CategoryDto> getAllCategoriesPublic(Integer from, Integer size);

    CategoryDto getCategoryByIdPublic(Long id);
}
