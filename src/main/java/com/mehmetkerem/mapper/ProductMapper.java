package com.mehmetkerem.mapper;

import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequest request);

    ProductResponse toResponse(Product entity);

    void update(@MappingTarget Product entity, ProductRequest request);
}
