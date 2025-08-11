package com.mehmetkerem.mapper;

import com.mehmetkerem.dto.request.CategoryRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import com.mehmetkerem.model.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category entity);

    void update(@MappingTarget Category entity, CategoryRequest request);
}
