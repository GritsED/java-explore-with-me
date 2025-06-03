package ru.practicum.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("[addCategoryAdmin] Admin attempts to add category {}", newCategory);
        String catName = newCategory.getName();
        checkExistCategoryByName(catName);

        Category category = categoryMapper.toEntity(newCategory);
        categoryRepository.save(category);
        log.info("Category added by admin: {}", category);
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public void deleteCategoryAdmin(Long id) {
        log.info("[deleteCategoryAdmin] Admin attempts to delete category with id {}", id);
        if (eventRepository.existsByCategoryId(id)) {
            log.warn("Cannot delete category with id {}: it is already used in events", id);
            throw new ConflictException("The category is not empty");
        }

        Category category = findCategoryByIdOrThrow(id);
        log.info("Category deleted by admin: {}", category);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public CategoryDto updateCategoryAdmin(CategoryDto categoryDto, Long id) {
        log.info("[updateCategoryAdmin] Admin attempts to update category with id {} using data: {}", id, categoryDto);
        Category category = findCategoryByIdOrThrow(id);
        String newName = categoryDto.getName();
        if (!category.getName().equals(newName)) {
            log.info("Category name will be changed from '{}' to '{}'", category.getName(), newName);
            checkExistCategoryByName(newName);
            category.setName(newName);
            categoryRepository.save(category);
            log.info("Category with id {} successfully updated", id);
        } else {
            log.info("No changes detected for category with id {}", id);
        }
        return categoryMapper.toDto(category);
    }

    @Override
    public List<CategoryDto> getAllCategoriesPublic(Integer from, Integer size) {
        log.info("[getAllCategoriesPublic] Public request to get categories with from={} and size={}", from, size);
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Category> categories = categoryRepository.findAll(pageable).getContent();

        log.info("Retrieved {} categories", categories.size());
        return categoryMapper.mapToCategoryDto(categories);
    }

    @Override
    public CategoryDto getCategoryByIdPublic(Long id) {
        log.info("[getCategoryByIdPublic] Public request to get category with id {}", id);
        Category category = findCategoryByIdOrThrow(id);
        return categoryMapper.toDto(category);
    }

    private void checkExistCategoryByName(String name) {
        boolean exists = categoryRepository.existsByName(name);
        if (exists) {
            log.warn("Category with name {} already exists", name);
            throw new ConflictException("Category name " + name + " already exists");
        }
    }

    private Category findCategoryByIdOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Category.class, id));
    }
}
