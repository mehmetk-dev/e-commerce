package com.mehmetkerem.mapper;

import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.model.Product;
import com.mehmetkerem.service.impl.CategoryServiceImpl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {


    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequest request);

    ProductResponse toResponse(Product entity);

    void update(@MappingTarget Product entity, ProductRequest request);

    default ProductResponse toResponseWithCategory(Product product, CategoryResponse categoryResponse){
        ProductResponse productResponse = toResponse(product);
        if (product!=null){
            productResponse.setCategory(categoryResponse);
        }
        return productResponse;
    }
}