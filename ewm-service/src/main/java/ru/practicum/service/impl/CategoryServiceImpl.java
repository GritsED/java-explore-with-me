package ru.practicum.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.NewCategoryDto;
import ru.practicum.dto.response.CategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.interfaces.CategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto addCategoryAdmin(NewCategoryDto newCategory) {
        String catName = newCategory.getName();
        checkExistCategoryByName(catName);

        Category category = categoryMapper.toEntity(newCategory);
        categoryRepository.save(category);
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public void deleteCategoryAdmin(Long id) {
        if (eventRepository.existsByCategoryId(id)) {
            throw new ConflictException("Can't delete a category that is in use.");
        }

        Category category = findCategoryByIdOrThrow(id);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public CategoryDto updateCategoryAdmin(CategoryDto categoryDto, Long id) {
        Category category = findCategoryByIdOrThrow(id);
        String newName = categoryDto.getName();
        if (!category.getName().equals(newName)) {
            checkExistCategoryByName(newName);
            category.setName(newName);
            categoryRepository.save(category);
        }
        return categoryMapper.toDto(category);
    }

    @Override
    public List<CategoryDto> getAllCategoriesPublic(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Category> categories = categoryRepository.findAll(pageable).getContent();

        return categoryMapper.mapToCategoryDto(categories);
    }

    @Override
    public CategoryDto getCategoryByIdPublic(Long id) {
        Category category = findCategoryByIdOrThrow(id);
        return categoryMapper.toDto(category);
    }

    private void checkExistCategoryByName(String name) {
        boolean exists = categoryRepository.existsByName(name);
        if (exists) {
            throw new ConflictException("Category name " + name + " already exists");
        }
    }

    private Category findCategoryByIdOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }
}
