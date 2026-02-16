package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.model.Product;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;

public interface IProductService {

    ProductResponse saveProduct(ProductRequest request);

    String deleteProduct(Long id);

    ProductResponse updateProduct(Long id, ProductRequest request);

    ProductResponse getProductResponseById(Long id);

    Product getProductById(Long id);

    List<ProductResponse> findAllProducts();

    List<ProductResponse> getProductResponsesByIds(List<Long> productIds);

    List<ProductResponse> getProductsByTitle(String title);

    List<ProductResponse> getProductsByCategory(Long categoryId);

    Page<ProductResponse> searchProducts(String title, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            Double minRating, Pageable pageable);

    Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String direction);
}
