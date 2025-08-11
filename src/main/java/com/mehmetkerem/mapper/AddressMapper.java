package com.mehmetkerem.mapper;

import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.model.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    Address toEntity(AddressRequest request);

    AddressResponse toResponse(Address address);

    void update(@MappingTarget Address entity, AddressRequest request);
}
