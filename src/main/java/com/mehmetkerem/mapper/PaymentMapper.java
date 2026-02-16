package com.mehmetkerem.mapper;

import com.mehmetkerem.dto.request.PaymentRequest;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.dto.response.PaymentResponse;
import com.mehmetkerem.model.Payment;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {
        OrderMapper.class }, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderId", ignore = true)
    Payment toEntity(PaymentRequest request);

    @Mapping(target = "order", ignore = true)
    PaymentResponse toResponse(Payment entity);

    @Mapping(target = "orderId", ignore = true)
    void update(@MappingTarget Payment entity, PaymentRequest request);

    default PaymentResponse toResponseWithOrder(Payment entity, OrderResponse order) {
        PaymentResponse resp = toResponse(entity);
        resp.setOrder(order);
        return resp;
    }
}
