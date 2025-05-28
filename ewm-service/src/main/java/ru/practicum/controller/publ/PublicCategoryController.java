package ru.practicum.controller.publ;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.response.CategoryDto;
import ru.practicum.service.interfaces.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class PublicCategoryController {
    private CategoryService categoryService;

    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable Long catId) {
        return categoryService.getCategoryByIdPublic(catId);
    }

    @GetMapping
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryService.getAllCategoriesPublic(from, size);
    }
}
