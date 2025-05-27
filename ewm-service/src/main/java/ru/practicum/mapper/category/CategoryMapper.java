package ru.practicum.mapper.category;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.request.NewCategoryDto;
import ru.practicum.dto.response.CategoryDto;
import ru.practicum.model.Category;

@Mapper
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category toEntity(NewCategoryDto newCategory);

    CategoryDto toDto(Category category);
}
