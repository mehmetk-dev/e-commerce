package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.response.CursorResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.util.ResultData;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

public interface IRestProductController {

    ResultData<ProductResponse> saveProduct(ProductRequest request);

    ResultData<List<ProductResponse>> findAllProducts();

    ResultData<ProductResponse> updateProduct(Long id, ProductRequest request);

    ResultData<String> deleteProduct(Long id);

    ResultData<ProductResponse> getProductById(Long id);

    ResultData<List<ProductResponse>> searchProductsByTitle(String title);

    ResultData<List<ProductResponse>> searchProductsByCategoryId(Long categoryId);

    ResultData<CursorResponse<ProductResponse>> searchProducts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction);
}
