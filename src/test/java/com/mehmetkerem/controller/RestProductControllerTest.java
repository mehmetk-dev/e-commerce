package com.mehmetkerem.controller;

import com.mehmetkerem.controller.impl.RestProductControllerImpl;
import com.mehmetkerem.dto.request.ProductRequest;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.service.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import com.mehmetkerem.util.ResultData;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class RestProductControllerTest {

    @Mock
    private IProductService productService;

    @InjectMocks
    private RestProductControllerImpl controller;

    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productRequest = new ProductRequest();
        productRequest.setTitle("Ürün");
        productRequest.setPrice(new BigDecimal("100"));
        productRequest.setStock(5);
        productRequest.setCategoryId(1L);
        productRequest.setImageUrls(List.of("http://img.com/1.jpg"));
        productResponse = ProductResponse.builder().id(1L).title("Ürün").price(new BigDecimal("100")).build();
    }

    @Test
    @DisplayName("findAllProducts - 200 ve liste döner")
    void findAllProducts_ShouldReturn200AndList() {
        when(productService.findAllProducts()).thenReturn(List.of(productResponse));

        ResultData<List<ProductResponse>> response = controller.findAllProducts();

        assertTrue(response.isStatus());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals("Ürün", response.getData().get(0).getTitle());
        verify(productService).findAllProducts();
    }

    @Test
    @DisplayName("saveProduct - 201 ve ürün döner")
    void saveProduct_ShouldReturn201AndProduct() {
        when(productService.saveProduct(any(ProductRequest.class))).thenReturn(productResponse);

        ResultData<ProductResponse> response = controller.saveProduct(productRequest);

        assertTrue(response.isStatus());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getId());
        verify(productService).saveProduct(productRequest);
    }

    @Test
    @DisplayName("getProductById - 200 ve ürün döner")
    void getProductById_ShouldReturn200AndProduct() {
        when(productService.getProductResponseById(1L)).thenReturn(productResponse);

        ResultData<ProductResponse> response = controller.getProductById(1L);

        assertTrue(response.isStatus());
        assertEquals(1L, response.getData().getId());
        verify(productService).getProductResponseById(1L);
    }

    @Test
    @DisplayName("searchProductsByTitle - başlığa göre liste döner")
    void searchProductsByTitle_ShouldReturnMatchingProducts() {
        when(productService.getProductsByTitle("Antika")).thenReturn(List.of(productResponse));

        ResultData<List<ProductResponse>> response = controller.searchProductsByTitle("Antika");

        assertTrue(response.isStatus());
        assertEquals(1, response.getData().size());
        verify(productService).getProductsByTitle("Antika");
    }

    @Test
    @DisplayName("searchProductsByCategoryId - kategoriye göre liste döner")
    void searchProductsByCategoryId_ShouldReturnProducts() {
        when(productService.getProductsByCategory(1L)).thenReturn(List.of(productResponse));

        ResultData<List<ProductResponse>> response = controller.searchProductsByCategoryId(1L);

        assertTrue(response.isStatus());
        assertEquals(1, response.getData().size());
        verify(productService).getProductsByCategory(1L);
    }

    @Test
    @DisplayName("updateProduct - 200 ve güncel ürün döner")
    void updateProduct_ShouldReturn200AndProduct() {
        when(productService.updateProduct(eq(1L), any(ProductRequest.class))).thenReturn(productResponse);

        ResultData<ProductResponse> response = controller.updateProduct(1L, productRequest);

        assertTrue(response.isStatus());
        verify(productService).updateProduct(1L, productRequest);
    }

    @Test
    @DisplayName("deleteProduct - 200 ve mesaj döner")
    void deleteProduct_ShouldReturn200AndMessage() {
        when(productService.deleteProduct(1L)).thenReturn("1 ID'li ürün silinmiştir!");

        ResultData<String> response = controller.deleteProduct(1L);

        assertTrue(response.isStatus());
        assertTrue(response.getData().contains("1"));
        verify(productService).deleteProduct(1L);
    }
}
