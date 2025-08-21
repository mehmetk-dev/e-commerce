package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.model.Product;

import java.util.List;

public interface IProductService {

    ProductResponse saveProduct(ProductRequest request);

    String deleteProduct(String id);

    ProductResponse updateProduct(String id, ProductRequest request);

    ProductResponse getProductResponseById(String id);

    Product getProductById(String id);

    List<ProductResponse> findAllProducts();

    List<ProductResponse> getProductsByIds(List<String> productIds );
}
