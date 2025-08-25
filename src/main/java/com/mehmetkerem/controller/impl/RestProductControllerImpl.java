package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestProductController;
import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.response.CursorResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.service.IProductService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
        return ResponseEntity.ok(productService.findAllProducts());
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id") String id, @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @GetMapping("/search/title")
    @Override
    public ResponseEntity<List<ProductResponse>> searchProductsByTitle(@RequestParam String title) {
        return ResponseEntity.ok(productService.getProductsByTitle(title));
    }

    @GetMapping("/search/category")
    @Override
    public ResponseEntity<List<ProductResponse>> searchProductsByCategoryId(@RequestParam String categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<String> deleteProduct(@PathVariable("id") String id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") String id) {
        return ResponseEntity.ok(productService.getProductResponseById(id));
    }

    @GetMapping
    public ResultData<CursorResponse<ProductResponse>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Page<ProductResponse> productPage = productService.getAllProducts(page, size, sortBy, direction);

        return ResultHelper.cursor(productPage);
    }
}
