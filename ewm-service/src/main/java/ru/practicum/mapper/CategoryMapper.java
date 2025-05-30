package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.request.NewCategoryDto;
import ru.practicum.dto.response.CategoryDto;
import ru.practicum.model.Category;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category toEntity(NewCategoryDto newCategory);

    CategoryDto toDto(Category category);

    List<CategoryDto> mapToCategoryDto(Iterable<Category> categories);
}
