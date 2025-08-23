package com.mehmetkerem.mapper;

import com.mehmetkerem.dto.request.CartItemRequest;
import com.mehmetkerem.dto.response.CartItemResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.model.CartItem;
import com.mehmetkerem.model.Product;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CartItemMapper {

    // Request → Entity
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", ignore = true)
    CartItem toEntity(CartItemRequest request);


    @Mapping(target = "product", ignore = true)
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "total", expression = "java(entity.getPrice().multiply(java.math.BigDecimal.valueOf(entity.getQuantity())))")
    CartItemResponse toResponse(CartItem entity);

    @Mapping(target = "price", ignore = true)
    void update(@MappingTarget CartItem entity, CartItemRequest request);

    default CartItemResponse toResponseWithProduct(CartItem entity, ProductResponse product) {
        CartItemResponse response = toResponse(entity);
        response.setProduct(product);
        return response;
    }

    default List<CartItem> toEntityCartItem(List<CartItemRequest> cartItemRequests) {

        return cartItemRequests.stream()
                .map(this::toEntity)
                .toList();
    }


}
