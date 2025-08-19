package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.CategoryRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.BaseException;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.CategoryMapper;
import com.mehmetkerem.model.Category;
import com.mehmetkerem.repository.CategoryRepository;
import com.mehmetkerem.service.ICategoryService;
import com.mehmetkerem.util.Messages;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryResponse saveCategory(CategoryRequest request) {

        if (categoryRepository.existsByName(request.getName())){
            throw new BadRequestException(String.format(ExceptionMessages.CATEGORY_ALL_READY_EXISTS,request.getName()));
        }

        Category savedCategory = categoryRepository.save(categoryMapper.toEntity(request));
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public String deleteCategory(String id) {
        categoryRepository.delete(getCategoryById(id));
        return String.format(Messages.DELETE_VALUE, id, "kategori");
    }

    @Override
    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        Category category = getCategoryById(id);
        categoryMapper.update(category, request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse getCategoryResponseById(String id) {
        return categoryMapper.toResponse(getCategoryById(id));
    }

    @Override
    public Category getCategoryById(String id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format(ExceptionMessages.NOT_FOUND, id, "kategori")));
    }

    @Override
    public List<CategoryResponse> findAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
}
