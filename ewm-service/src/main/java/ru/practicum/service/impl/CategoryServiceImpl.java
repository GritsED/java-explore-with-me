package ru.practicum.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.NewCategoryDto;
import ru.practicum.dto.response.CategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.category.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.service.interfaces.CategoryService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategory) {
        String catName = newCategory.getName();
        checkExistCategoryByName(catName);

        Category category = categoryMapper.toEntity(newCategory);
        categoryRepository.save(category);
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        /**
         * Тут нужно добавить проверку, что с категорией не связано ни одно событие
         * И выбросить исключение Конфликт 409
         */
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(CategoryDto categoryDto, Long id) {
        Category category = findCategoryById(id);
        String newName = categoryDto.getName();
        if (!category.getName().equals(newName)) {
            checkExistCategoryByName(newName);
            category.setName(newName);
            categoryRepository.save(category);
        }
        return categoryMapper.toDto(category);
    }

    private void checkExistCategoryByName(String name) {
        boolean exists = categoryRepository.existsByName(name);
        if (exists) {
            throw new ConflictException("Category name " + name + " already exists");
        }
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }
}
