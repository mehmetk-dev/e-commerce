package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.CategoryRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import com.mehmetkerem.model.Category;

import java.util.List;

public interface ICategoryService {
    CategoryResponse saveCategory(CategoryRequest request);

    String deleteCategory(Long id);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    CategoryResponse getCategoryResponseById(Long id);

    Category getCategoryById(Long id);

    List<CategoryResponse> findAllCategories();
}
