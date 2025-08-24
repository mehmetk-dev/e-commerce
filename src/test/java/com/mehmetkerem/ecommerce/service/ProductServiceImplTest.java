package com.mehmetkerem.ecommerce.service;

import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.ProductMapper;
import com.mehmetkerem.model.Product;
import com.mehmetkerem.repository.ProductRepository;
import com.mehmetkerem.service.ICategoryService;
import com.mehmetkerem.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ICategoryService categoryService;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveProduct_success() {
        ProductRequest request = new ProductRequest();
        request.setCategoryId("cat1");
        Product product = new Product();
        ProductResponse response = new ProductResponse();
        CategoryResponse categoryResponse = new CategoryResponse();

        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(categoryService.getCategoryResponseById("cat1")).thenReturn(categoryResponse);
        when(productMapper.toResponseWithCategory(product, categoryResponse)).thenReturn(response);

        ProductResponse result = productService.saveProduct(request);
        assertNotNull(result);
    }

    @Test
    void testGetProductById_success() {
        Product product = new Product();
        product.setId("1");
        when(productRepository.findById("1")).thenReturn(Optional.of(product));

        Product result = productService.getProductById("1");
        assertEquals("1", result.getId());
    }

    @Test
    void testGetProductById_notFound() {
        when(productRepository.findById("2")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductById("2"));
    }

    @Test
    void testGetProductResponseById_success() {
        Product product = new Product();
        product.setId("1");
        product.setCategoryId("cat1");
        ProductResponse response = new ProductResponse();
        CategoryResponse categoryResponse = new CategoryResponse();

        when(productRepository.findById("1")).thenReturn(Optional.of(product));
        when(categoryService.getCategoryResponseById("cat1")).thenReturn(categoryResponse);
        when(productMapper.toResponseWithCategory(product, categoryResponse)).thenReturn(response);

        ProductResponse result = productService.getProductResponseById("1");
        assertNotNull(result);
    }

    @Test
    void testUpdateProduct_success() {
        ProductRequest request = new ProductRequest();
        Product product = new Product();
        product.setCategoryId("cat1");
        ProductResponse response = new ProductResponse();
        CategoryResponse categoryResponse = new CategoryResponse();

        when(productRepository.findById("1")).thenReturn(Optional.of(product));
        doNothing().when(productMapper).update(product, request);
        when(productRepository.save(product)).thenReturn(product);
        when(categoryService.getCategoryResponseById("cat1")).thenReturn(categoryResponse);
        when(productMapper.toResponseWithCategory(product, categoryResponse)).thenReturn(response);

        ProductResponse result = productService.updateProduct("1", request);
        assertNotNull(result);
    }

    @Test
    void testDeleteProduct_success() {
        Product product = new Product();
        when(productRepository.findById("1")).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        String result = productService.deleteProduct("1");
        assertTrue(result.contains("1"));
    }

    @Test
    void testDeleteProduct_notFound() {
        when(productRepository.findById("2")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.deleteProduct("2"));
    }

    @Test
    void testFindAllProducts_success() {
        Product product1 = new Product();
        product1.setCategoryId("cat1");
        Product product2 = new Product();
        product2.setCategoryId("cat2");
        ProductResponse response1 = new ProductResponse();
        ProductResponse response2 = new ProductResponse();
        CategoryResponse category1 = new CategoryResponse();
        CategoryResponse category2 = new CategoryResponse();

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));
        when(categoryService.getCategoryResponseById("cat1")).thenReturn(category1);
        when(categoryService.getCategoryResponseById("cat2")).thenReturn(category2);
        when(productMapper.toResponseWithCategory(product1, category1)).thenReturn(response1);
        when(productMapper.toResponseWithCategory(product2, category2)).thenReturn(response2);

        List<ProductResponse> results = productService.findAllProducts();
        assertEquals(2, results.size());
    }

    @Test
    void testGetProductsByTitle_success() {
        Product product = new Product();
        product.setCategoryId("cat1");
        ProductResponse response = new ProductResponse();
        CategoryResponse categoryResponse = new CategoryResponse();

        when(productRepository.findByTitleContainingIgnoreCase("title")).thenReturn(List.of(product));
        when(categoryService.getCategoryResponseById("cat1")).thenReturn(categoryResponse);
        when(productMapper.toResponseWithCategory(product, categoryResponse)).thenReturn(response);

        List<ProductResponse> results = productService.getProductsByTitle("title");
        assertEquals(1, results.size());
    }

    @Test
    void testGetProductsByCategory_success() {
        Product product = new Product();
        product.setCategoryId("cat1");
        ProductResponse response = new ProductResponse();
        CategoryResponse categoryResponse = new CategoryResponse();

        when(productRepository.findByCategoryId("cat1")).thenReturn(List.of(product));
        when(categoryService.getCategoryResponseById("cat1")).thenReturn(categoryResponse);
        when(productMapper.toResponseWithCategory(product, categoryResponse)).thenReturn(response);

        List<ProductResponse> results = productService.getProductsByCategory("cat1");
        assertEquals(1, results.size());
    }

    @Test
    void testGetProductResponsesByIds_success() {
        Product product = new Product();
        product.setCategoryId("cat1");
        ProductResponse response = new ProductResponse();
        CategoryResponse categoryResponse = new CategoryResponse();

        when(productRepository.findByIdIn(List.of("1"))).thenReturn(List.of(product));
        when(categoryService.getCategoryResponseById("cat1")).thenReturn(categoryResponse);
        when(productMapper.toResponseWithCategory(product, categoryResponse)).thenReturn(response);

        List<ProductResponse> results = productService.getProductResponsesByIds(List.of("1"));
        assertEquals(1, results.size());
    }
}
