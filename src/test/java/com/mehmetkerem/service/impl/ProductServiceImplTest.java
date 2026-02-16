package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.ProductMapper;
import com.mehmetkerem.model.Product;
import com.mehmetkerem.repository.ProductRepository;
import com.mehmetkerem.service.ICategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ICategoryService categoryService;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductRequest productRequest;
    private Product product;
    private ProductResponse productResponse;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        productRequest = new ProductRequest();
        productRequest.setTitle("Antika Saat");
        productRequest.setDescription("19. yy");
        productRequest.setPrice(new BigDecimal("1500"));
        productRequest.setStock(10);
        productRequest.setCategoryId(1L);
        productRequest.setImageUrls(List.of("http://img.com/1.jpg"));

        product = Product.builder()
                .id(1L)
                .title("Antika Saat")
                .price(new BigDecimal("1500"))
                .stock(10)
                .categoryId(1L)
                .build();

        productResponse = ProductResponse.builder()
                .id(1L)
                .title("Antika Saat")
                .price(new BigDecimal("1500"))
                .stock(10)
                .build();

        categoryResponse = new CategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Antika");
    }

    @Test
    @DisplayName("saveProduct - ürün kaydedilir ve response döner")
    void saveProduct_ShouldSaveAndReturnResponse() {
        when(productMapper.toEntity(productRequest)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(categoryService.getCategoryResponseById(1L)).thenReturn(categoryResponse);
        when(productMapper.toResponseWithCategory(product, categoryResponse)).thenReturn(productResponse);

        ProductResponse result = productService.saveProduct(productRequest);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
        verify(categoryService).getCategoryResponseById(1L);
    }

    @Test
    @DisplayName("getProductById - mevcut ürün döner")
    void getProductById_WhenExists_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Antika Saat", result.getTitle());
    }

    @Test
    @DisplayName("getProductById - olmayan id ile NotFoundException fırlatır")
    void getProductById_WhenNotExists_ShouldThrowNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    @DisplayName("getProductResponseById - response döner")
    void getProductResponseById_ShouldReturnResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryService.getCategoryResponseById(1L)).thenReturn(categoryResponse);
        when(productMapper.toResponseWithCategory(product, categoryResponse)).thenReturn(productResponse);

        ProductResponse result = productService.getProductResponseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("deleteProduct - ürün silinir")
    void deleteProduct_WhenExists_ShouldDelete() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        String result = productService.deleteProduct(1L);

        assertTrue(result.contains("1"));
        assertTrue(result.contains("ürün"));
        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("updateProduct - ürün güncellenir")
    void updateProduct_WhenExists_ShouldUpdateAndReturn() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(categoryService.getCategoryResponseById(1L)).thenReturn(categoryResponse);
        when(productMapper.toResponseWithCategory(product, categoryResponse)).thenReturn(productResponse);

        productService.updateProduct(1L, productRequest);

        verify(productMapper).update(eq(product), eq(productRequest));
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("findAllProducts - tüm ürünler listelenir")
    void findAllProducts_ShouldReturnAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(categoryService.getCategoryResponseById(1L)).thenReturn(categoryResponse);
        when(productMapper.toResponseWithCategory(product, categoryResponse)).thenReturn(productResponse);

        List<ProductResponse> result = productService.findAllProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    @DisplayName("getProductsByTitle - başlığa göre ürünler döner")
    void getProductsByTitle_ShouldReturnMatchingProducts() {
        when(productRepository.findByTitleContainingIgnoreCase("Antika")).thenReturn(List.of(product));
        when(categoryService.getCategoryResponseById(1L)).thenReturn(categoryResponse);
        when(productMapper.toResponseWithCategory(product, categoryResponse)).thenReturn(productResponse);

        List<ProductResponse> result = productService.getProductsByTitle("Antika");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Antika Saat", result.get(0).getTitle());
    }

    @Test
    @DisplayName("getProductsByCategory - kategoriye göre ürünler döner")
    void getProductsByCategory_ShouldReturnProductsByCategory() {
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(product));
        when(categoryService.getCategoryResponseById(1L)).thenReturn(categoryResponse);
        when(productMapper.toResponseWithCategory(product, categoryResponse)).thenReturn(productResponse);

        List<ProductResponse> result = productService.getProductsByCategory(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getAllProducts - sayfalı liste döner")
    void getAllProducts_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findAll(any(PageRequest.class))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        Page<ProductResponse> result = productService.getAllProducts(0, 10, "id", "asc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }
}
