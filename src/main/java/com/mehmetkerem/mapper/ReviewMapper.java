package com.mehmetkerem.mapper;

import com.mehmetkerem.dto.request.ReviewRequest;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.dto.response.ReviewResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.model.Review;
import com.mehmetkerem.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Review toEntity(ReviewRequest request);

    ReviewResponse toResponse(Review entity);

    void update(@MappingTarget Review entity, ReviewRequest request);

    default ReviewResponse toResponseWithDetails(Review entity, ProductResponse product, UserResponse  user) {
        ReviewResponse response = toResponse(entity);
        response.setProduct(product);
        response.setUser(user);
        return response;
    }
}
