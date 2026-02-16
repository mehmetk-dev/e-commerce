package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.CategoryRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.CategoryMapper;
import com.mehmetkerem.model.Category;
import com.mehmetkerem.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryRequest categoryRequest;
    private Category category;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Antika");

        category = Category.builder()
                .id(1L)
                .name("Antika")
                .deleted(false)
                .build();

        categoryResponse = new CategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Antika");
    }

    @Test
    @DisplayName("saveCategory - yeni kategori başarıyla kaydedilir")
    void saveCategory_WhenNameNotExists_ShouldSaveAndReturnResponse() {
        when(categoryRepository.existsByName("Antika")).thenReturn(false);
        when(categoryMapper.toEntity(categoryRequest)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.saveCategory(categoryRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Antika", result.getName());
        verify(categoryRepository).existsByName("Antika");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("saveCategory - aynı isimde kategori varsa BadRequestException fırlatır")
    void saveCategory_WhenNameExists_ShouldThrowBadRequestException() {
        when(categoryRepository.existsByName("Antika")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.saveCategory(categoryRequest));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("getCategoryById - mevcut id ile kategori döner")
    void getCategoryById_WhenExists_ShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Antika", result.getName());
    }

    @Test
    @DisplayName("getCategoryById - olmayan id ile NotFoundException fırlatır")
    void getCategoryById_WhenNotExists_ShouldThrowNotFoundException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.getCategoryById(999L));
    }

    @Test
    @DisplayName("getCategoryResponseById - response döner")
    void getCategoryResponseById_ShouldReturnResponse() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.getCategoryResponseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Antika", result.getName());
    }

    @Test
    @DisplayName("updateCategory - güncelleme başarılı")
    void updateCategory_WhenExists_ShouldUpdateAndReturn() {
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Antika Saatler");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        categoryService.updateCategory(1L, updateRequest);

        verify(categoryMapper).update(eq(category), eq(updateRequest));
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("deleteCategory - kategori silinir ve mesaj döner")
    void deleteCategory_WhenExists_ShouldDeleteAndReturnMessage() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(category);

        String result = categoryService.deleteCategory(1L);

        assertTrue(result.contains("1"));
        assertTrue(result.contains("kategori"));
        verify(categoryRepository).delete(category);
    }

    @Test
    @DisplayName("findAllCategories - tüm kategoriler listelenir")
    void findAllCategories_ShouldReturnAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        List<CategoryResponse> result = categoryService.findAllCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Antika", result.get(0).getName());
    }
}
