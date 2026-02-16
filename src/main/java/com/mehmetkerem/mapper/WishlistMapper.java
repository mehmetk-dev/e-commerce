package com.mehmetkerem.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.mehmetkerem.dto.response.WishlistItemResponse;
import com.mehmetkerem.dto.response.WishlistResponse;
import com.mehmetkerem.model.WishList;
import com.mehmetkerem.model.WishlistItem;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WishlistMapper {

    // WishList entity'sini alıp WishlistResponse DTO'suna çevirir
    WishlistResponse toResponse(WishList wishlist);

    // WishlistItem entity'sini alıp WishlistItemResponse DTO'suna çevirir
    // (Bunu MapStruct, toResponse içinde otomatik olarak kullanır)
    WishlistItemResponse toItemResponse(WishlistItem item);
}
