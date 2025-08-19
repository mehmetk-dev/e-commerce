package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestCategoryController;
import com.mehmetkerem.dto.request.CategoryRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import com.mehmetkerem.service.ICategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/category")
public class RestCategoryControllerImpl implements IRestCategoryController {

    private final ICategoryService categoryService;

    public RestCategoryControllerImpl(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/save")
    @Override
    public ResponseEntity<CategoryResponse> saveCategory(@RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.saveCategory(request));
    }

    @DeleteMapping("{id}")
    @Override
    public ResponseEntity<String> deleteCategory(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.deleteCategory(id));
    }

    @GetMapping("{id}")
    @Override
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.getCategoryResponseById(id));
    }

    @PutMapping("{id}")
    @Override
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable("id") String id, @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.updateCategory(id, request));
    }

    @GetMapping("/find-all")
    @Override
    public ResponseEntity<List<CategoryResponse>> findAllCategories() {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.findAllCategories());
    }
}
