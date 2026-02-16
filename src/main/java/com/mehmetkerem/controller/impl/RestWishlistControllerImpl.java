package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestWishlistController;
import com.mehmetkerem.dto.response.WishlistResponse;
import com.mehmetkerem.service.IWishlistService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import com.mehmetkerem.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/wishlist")
@RequiredArgsConstructor
public class RestWishlistControllerImpl implements IRestWishlistController {

    private final IWishlistService wishlistService;

    private static long requireCurrentUserId() {
        Long id = SecurityUtils.getCurrentUserId();
        if (id == null) {
            throw new InsufficientAuthenticationException("Oturum gerekli");
        }
        return id;
    }

    @Override
    @GetMapping
    public ResultData<WishlistResponse> getWishlist() {
        return ResultHelper.success(wishlistService.getWishlistByUserId(requireCurrentUserId()));
    }

    @Override
    @PostMapping("/add/{productId}")
    public ResultData<WishlistResponse> addItem(@PathVariable Long productId) {
        return ResultHelper.success(wishlistService.addItemToWishlist(requireCurrentUserId(), productId));
    }

    @Override
    @DeleteMapping("/remove/{productId}")
    public ResultData<String> removeItem(@PathVariable Long productId) {
        wishlistService.removeItemFromWishlist(requireCurrentUserId(), productId);
        return ResultHelper.success("Ürün favorilerden kaldırıldı.");
    }

    @Override
    @DeleteMapping("/clear")
    public ResultData<String> clearWishlist() {
        wishlistService.clearWishlist(requireCurrentUserId());
        return ResultHelper.success("Favori listesi temizlendi.");
    }
}
