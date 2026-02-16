package com.mehmetkerem.controller;

import com.mehmetkerem.controller.impl.RestCategoryControllerImpl;
import com.mehmetkerem.dto.request.CategoryRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import com.mehmetkerem.service.ICategoryService;
import com.mehmetkerem.util.ResultData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestCategoryControllerTest {

    @Mock
    private ICategoryService categoryService;

    @InjectMocks
    private RestCategoryControllerImpl controller;

    private CategoryRequest categoryRequest;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Antika");
        categoryResponse = new CategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Antika");
    }

    @Test
    @DisplayName("findAllCategories - 200 ve liste döner")
    void findAllCategories_ShouldReturn200AndList() {
        when(categoryService.findAllCategories()).thenReturn(List.of(categoryResponse));

        ResultData<List<CategoryResponse>> response = controller.findAllCategories();

        assertTrue(response.isStatus());
        List<CategoryResponse> body = response.getData();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals("Antika", body.get(0).getName());
        verify(categoryService).findAllCategories();
    }

    @Test
    @DisplayName("getCategoryById - 200 ve kategori döner")
    void getCategoryById_ShouldReturn200AndCategory() {
        when(categoryService.getCategoryResponseById(1L)).thenReturn(categoryResponse);

        ResultData<CategoryResponse> response = controller.getCategoryById(1L);

        assertTrue(response.isStatus());
        CategoryResponse body = response.getData();
        assertNotNull(body);
        assertEquals(1L, body.getId());
        assertEquals("Antika", body.getName());
        verify(categoryService).getCategoryResponseById(1L);
    }

    @Test
    @DisplayName("saveCategory - 201 ve kaydedilmiş kategori döner")
    void saveCategory_ShouldReturn201AndCategory() {
        when(categoryService.saveCategory(any(CategoryRequest.class))).thenReturn(categoryResponse);

        ResultData<CategoryResponse> response = controller.saveCategory(categoryRequest);

        assertTrue(response.isStatus());
        CategoryResponse body = response.getData();
        assertNotNull(body);
        assertEquals(1L, body.getId());
        verify(categoryService).saveCategory(categoryRequest);
    }

    @Test
    @DisplayName("updateCategory - 200 ve güncel kategori döner")
    void updateCategory_ShouldReturn200AndCategory() {
        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class))).thenReturn(categoryResponse);

        ResultData<CategoryResponse> response = controller.updateCategory(1L, categoryRequest);

        assertTrue(response.isStatus());
        assertNotNull(response.getData());
        verify(categoryService).updateCategory(1L, categoryRequest);
    }

    @Test
    @DisplayName("deleteCategory - 200 ve mesaj döner")
    void deleteCategory_ShouldReturn200AndMessage() {
        when(categoryService.deleteCategory(1L)).thenReturn("1 ID'li kategori silinmiştir!");

        ResultData<String> response = controller.deleteCategory(1L);

        assertTrue(response.isStatus());
        String body = response.getData();
        assertNotNull(body);
        assertTrue(body.contains("1"));
        verify(categoryService).deleteCategory(1L);
    }
}
