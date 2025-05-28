package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.NewCategoryDto;
import ru.practicum.dto.response.CategoryDto;
import ru.practicum.service.interfaces.CategoryService;

@Validated
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    CategoryDto createCategory(@RequestBody @Valid NewCategoryDto newCategory) {
        return categoryService.addCategoryAdmin(newCategory);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategoryAdmin(catId);
    }

    @PatchMapping("/{catId}")
    @ResponseStatus(value = HttpStatus.OK)
    CategoryDto updateCategory(@PathVariable Long catId, @RequestBody @Valid CategoryDto categoryDto) {
        return categoryService.updateCategoryAdmin(categoryDto, catId);
    }
}
