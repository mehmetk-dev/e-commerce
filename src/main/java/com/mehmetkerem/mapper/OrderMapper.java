// mapper/OrderMapper.java
package com.mehmetkerem.mapper;

import com.mehmetkerem.dto.request.OrderRequest;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.model.Order;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {OrderItemMapper.class, AddressMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "shippingAddress", ignore = true)
    Order toEntity(OrderRequest request);

    @Mapping(target = "user", ignore = true)
    OrderResponse toResponse(Order entity);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "shippingAddress", ignore = true)
    void update(@MappingTarget Order entity, OrderRequest request);

    default OrderResponse toResponseWithUser(Order entity, UserResponse user) {
        OrderResponse resp = toResponse(entity);
        resp.setUser(user);
        return resp;
    }
}
