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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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
    @Transactional
    @CacheEvict(cacheNames = {"products:list", "products:byId"}, allEntries = true)
    public ProductResponse saveProduct(ProductRequest request) {
        return productMapper.toResponseWithCategory(productRepository.save(productMapper.toEntity(request)),
                categoryService.getCategoryResponseById(request.getCategoryId()));
    }

    @CacheEvict(cacheNames = {"products:list", "products:byId"}, allEntries = true)
    @Override
    public String deleteProduct(String id) {
        productRepository.delete(getProductById(id));
        return String.format(Messages.DELETE_VALUE, id, "ürün");
    }

    @Override
    @CacheEvict(cacheNames = {"products:list", "products:byId"}, allEntries = true)
    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product product = getProductById(id);
        productMapper.update(product, request);
        return productMapper.toResponseWithCategory(productRepository.save(product),
                categoryService.getCategoryResponseById(product.getCategoryId()));
    }

    @Override
    @Cacheable(cacheNames = "products:byId", key = "#id")
    public ProductResponse getProductResponseById(String id) {
        Product product = getProductById(id);
        return productMapper.toResponseWithCategory(
                product, categoryService.getCategoryResponseById(product.getCategoryId()));
    }

    @Override
    public Product getProductById(String id) {
        return productRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format(ExceptionMessages.NOT_FOUND, id, "ürün")));
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

    @Override
    public List<ProductResponse> getProductResponsesByIds(List<String> productIds) {
        return getProductsByIds(productIds).stream().map(this::mapProductWithCategory).toList();
    }

    public List<Product> getProductsByIds(List<String> productIds) {
        return productRepository.findByIdIn(productIds);
    }

    public List<Product> saveAllProducts(List<Product> products) {
        return productRepository.saveAll(products);
    }

    @Override
    public List<ProductResponse> getProductsByTitle(String title) {
        List<Product> products = productRepository.findByTitleContainingIgnoreCase(title);

        return products.stream().map(this::mapProductWithCategory).toList();
    }

    @Override
    public List<ProductResponse> getProductsByCategory(String categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);

        return products.stream()
                .map(this::mapProductWithCategory)
                .toList();
    }

    @Cacheable(
            cacheNames = "products:list",
            key = "'p='+#page+';s='+#size+';sort='+#sortBy+';dir='+#direction"
    )
    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageable = PageRequest.of(page, size, sort);

        return productRepository.findAll(pageable).map(productMapper::toResponse);
    }

    private ProductResponse mapProductWithCategory(Product product) {
        CategoryResponse categoryResponse = null;
        if (product.getCategoryId() != null) {
            categoryResponse = categoryService.getCategoryResponseById(product.getCategoryId());
        }
        return productMapper.toResponseWithCategory(product, categoryResponse);
    }
}
