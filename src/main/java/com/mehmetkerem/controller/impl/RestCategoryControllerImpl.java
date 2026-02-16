package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestCategoryController;
import com.mehmetkerem.dto.request.CategoryRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import com.mehmetkerem.service.ICategoryService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
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
    public ResultData<CategoryResponse> saveCategory(@RequestBody CategoryRequest request) {
        return ResultHelper.success(categoryService.saveCategory(request));
    }

    @DeleteMapping("{id}")
    @Override
    public ResultData<String> deleteCategory(@PathVariable("id") Long id) {
        return ResultHelper.success(categoryService.deleteCategory(id));
    }

    @GetMapping("{id}")
    @Override
    public ResultData<CategoryResponse> getCategoryById(@PathVariable("id") Long id) {
        return ResultHelper.success(categoryService.getCategoryResponseById(id));
    }

    @PutMapping("{id}")
    @Override
    public ResultData<CategoryResponse> updateCategory(@PathVariable("id") Long id,
            @RequestBody CategoryRequest request) {
        return ResultHelper.success(categoryService.updateCategory(id, request));
    }

    @GetMapping("/find-all")
    @Override
    public ResultData<List<CategoryResponse>> findAllCategories() {
        return ResultHelper.success(categoryService.findAllCategories());
    }
}
