package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestProductController;
import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.service.IProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/product")
public class RestProductControllerImpl implements IRestProductController {

    private final IProductService productService;

    public RestProductControllerImpl(IProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/save")
    @Override
    public ResponseEntity<ProductResponse> saveProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.saveProduct(request));
    }

    @GetMapping("/find-all")
    @Override
    public ResponseEntity<List<ProductResponse>> findAllProducts() {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.findAllProducts());

    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id")String id,@RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<String> deleteProduct(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.deleteProduct(id));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.getProductResponseById(id));
    }
}
