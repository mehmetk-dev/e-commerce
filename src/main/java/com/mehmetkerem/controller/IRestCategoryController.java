package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.CategoryRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IRestCategoryController {

    ResponseEntity<CategoryResponse> saveCategory(CategoryRequest request);
    ResponseEntity<String> deleteCategory(String id);
    ResponseEntity<CategoryResponse> getCategoryById(String id);
    ResponseEntity<CategoryResponse> updateCategory(String id,CategoryRequest request);
    ResponseEntity<List<CategoryResponse>> findAllCategories();
}
