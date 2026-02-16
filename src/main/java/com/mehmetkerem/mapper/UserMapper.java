package com.mehmetkerem.mapper;

import com.mehmetkerem.dto.request.UserRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserRequest request);

    @Mapping(target = "addresses", ignore = true)
    UserResponse toResponse(User entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void update(@MappingTarget User entity, UserRequest request);

    default UserResponse toResponseWithAddresses(User entity, List<AddressResponse> addresses) {
        UserResponse resp = toResponse(entity);
        resp.setAddresses(addresses);
        return resp;
    }
}
