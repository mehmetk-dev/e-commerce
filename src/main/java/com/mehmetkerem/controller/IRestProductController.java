package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.response.ProductResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IRestProductController {

    ResponseEntity<ProductResponse> saveProduct(ProductRequest request);

    ResponseEntity<List<ProductResponse>> findAllProducts();

    ResponseEntity<ProductResponse> updateProduct(String id, ProductRequest request);

    ResponseEntity<String> deleteProduct(String id);

    ResponseEntity<ProductResponse> getProductById(String id);
}
