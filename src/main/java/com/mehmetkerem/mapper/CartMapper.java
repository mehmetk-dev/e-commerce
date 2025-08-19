package com.mehmetkerem.mapper;

import com.mehmetkerem.dto.request.CartRequest;
import com.mehmetkerem.dto.response.CartResponse;
import com.mehmetkerem.model.Cart;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { CartItemMapper.class }, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CartMapper {

    @Mapping(target = "id", ignore = true)
    Cart toEntity(CartRequest request);

    CartResponse toResponse(Cart cart);

    void update(@MappingTarget Cart entity, CartRequest request);


}
