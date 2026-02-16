package com.mehmetkerem.mapper;

import com.mehmetkerem.dto.request.OrderItemRequest;
import com.mehmetkerem.dto.response.OrderItemResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.model.OrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
        ProductMapper.class })
public interface OrderItemMapper {

    OrderItem toEntity(OrderItemRequest request);

    @Mapping(target = "product", ignore = true)
    OrderItemResponse toResponse(OrderItem entity);

    void update(@MappingTarget OrderItem entity, OrderItemRequest request);

    default OrderItemResponse toResponseWithProduct(OrderItem entity, ProductResponse product) {
        OrderItemResponse response = toResponse(entity);
        if (product != null) {
            response.setProduct(product);
        }
        return response;
    }
}
