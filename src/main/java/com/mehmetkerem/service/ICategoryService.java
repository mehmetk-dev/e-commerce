package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.CategoryRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import com.mehmetkerem.model.Category;

import java.util.List;

public interface ICategoryService {
    CategoryResponse saveCategory(CategoryRequest request);

    String deleteCategory(String id);

    CategoryResponse updateCategory(String id, CategoryRequest request);

    CategoryResponse getCategoryResponseById(String id);

    Category getCategoryById(String id);

    List<CategoryResponse> findAllCategories();
}
