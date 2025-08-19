package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.ProductMapper;
import com.mehmetkerem.model.Product;
import com.mehmetkerem.repository.ProductRepository;
import com.mehmetkerem.service.ICategoryService;
import com.mehmetkerem.service.IProductService;
import com.mehmetkerem.util.Messages;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ICategoryService categoryService;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper, ICategoryService categoryService) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.categoryService = categoryService;
    }

    @Override
    public ProductResponse saveProduct(ProductRequest request) {
        return productMapper.toResponseWithCategory(
                productRepository.save(productMapper.toEntity(request)),
                categoryService.getCategoryResponseById(request.getCategoryId()));
    }

    @Override
    public String deleteProduct(String id) {
        productRepository.delete(getProductById(id));
        return String.format(Messages.DELETE_VALUE,id,"ürün");
    }

    @Override
    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product product = getProductById(id);
        productMapper.update(product,request);
        return productMapper.toResponseWithCategory(productRepository.save(product),
                categoryService.getCategoryResponseById(product.getCategoryId()));
    }

    @Override
    public ProductResponse getProductResponseById(String id) {
        Product product = getProductById(id);
        return productMapper.toResponseWithCategory(
                product,categoryService.getCategoryResponseById(product.getCategoryId()));
    }

    @Override
    public Product getProductById(String id) {
        return productRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format(ExceptionMessages.NOT_FOUND,id,"ürün")));
    }

    @Override
    public List<ProductResponse> findAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> {
                    CategoryResponse categoryResponse = categoryService.getCategoryResponseById(product.getCategoryId());
                    return productMapper.toResponseWithCategory(product, categoryResponse);
                })
                .toList();
    }
}
