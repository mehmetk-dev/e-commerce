package com.mehmetkerem.SpringMVCBackEnd.service;

import com.mehmetkerem.dto.request.CategoryRequest;
import com.mehmetkerem.dto.response.CategoryResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.CategoryMapper;
import com.mehmetkerem.model.Category;
import com.mehmetkerem.repository.CategoryRepository;
import com.mehmetkerem.service.impl.CategoryServiceImpl;
import com.mehmetkerem.util.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // mock'ları initialize et
    }

    // saveCategory: isim benzersizse kategori kaydedilir ve response döner
    @Test
    void saveCategory_success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");

        Category entity = new Category();
        Category saved = new Category();
        CategoryResponse response = new CategoryResponse();

        when(categoryRepository.existsByName("Electronics")).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(entity);
        when(categoryRepository.save(entity)).thenReturn(saved);
        when(categoryMapper.toResponse(saved)).thenReturn(response);

        CategoryResponse result = categoryService.saveCategory(request);

        assertNotNull(result); // kayıt sonrası response dönmeli
        verify(categoryRepository).save(entity); // repository save çağrılmalı
    }

    // saveCategory: isim zaten varsa BadRequestException atılmalı
    @Test
    void saveCategory_nameExists_throwsBadRequest() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Existing");

        when(categoryRepository.existsByName("Existing")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.saveCategory(request)); // isim çakışması
        verify(categoryRepository, never()).save(any()); // kayıt olmamalı
    }

    // deleteCategory: mevcut kategori silinir ve mesaj döner
    @Test
    void deleteCategory_success() {
        String id = "c1";
        Category category = new Category();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        String result = categoryService.deleteCategory(id);

        String expected = String.format(Messages.DELETE_VALUE, id, "kategori");
        assertEquals(expected, result); // mesaj formatı beklenen gibi olmalı
        verify(categoryRepository).delete(category); // delete çağrılmalı
    }

    // updateCategory: mevcut kategori güncellenir ve response döner
    @Test
    void updateCategory_success() {
        String id = "c1";
        CategoryRequest request = new CategoryRequest();

        Category entity = new Category();
        Category saved = new Category();
        CategoryResponse response = new CategoryResponse();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(entity));
        // mapper.update(...) void ise doNothing yeterli
        doNothing().when(categoryMapper).update(entity, request);
        when(categoryRepository.save(entity)).thenReturn(saved);
        when(categoryMapper.toResponse(saved)).thenReturn(response);

        CategoryResponse result = categoryService.updateCategory(id, request);

        assertNotNull(result); // güncelleme sonrası response null olmamalı
        verify(categoryMapper).update(entity, request); // update çağrısı yapılmalı
        verify(categoryRepository).save(entity); // ardından save edilmeli
    }

    // getCategoryResponseById: bulunan entity response'a maplenir
    @Test
    void getCategoryResponseById_success() {
        String id = "c1";
        Category entity = new Category();
        CategoryResponse response = new CategoryResponse();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(entity));
        when(categoryMapper.toResponse(entity)).thenReturn(response);

        CategoryResponse result = categoryService.getCategoryResponseById(id);

        assertNotNull(result); // response dönmeli
    }

    // getCategoryById: kategori bulunursa entity dönmeli
    @Test
    void getCategoryById_success() {
        String id = "c1";
        Category entity = new Category();
        entity.setId(id);

        when(categoryRepository.findById(id)).thenReturn(Optional.of(entity));

        Category result = categoryService.getCategoryById(id);

        assertEquals(id, result.getId()); // id eşleşmeli
    }

    // getCategoryById: bulunamazsa NotFoundException atılmalı
    @Test
    void getCategoryById_notFound() {
        when(categoryRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.getCategoryById("missing")); // bulunamayan id
    }

    // findAllCategories: tüm kategoriler response listesine maplenmeli
    @Test
    void findAllCategories_success() {
        Category c1 = new Category();
        Category c2 = new Category();
        CategoryResponse r1 = new CategoryResponse();
        CategoryResponse r2 = new CategoryResponse();

        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));
        when(categoryMapper.toResponse(c1)).thenReturn(r1);
        when(categoryMapper.toResponse(c2)).thenReturn(r2);

        List<CategoryResponse> results = categoryService.findAllCategories();

        assertEquals(2, results.size()); // 2 kategori dönmeli
        assertTrue(results.containsAll(List.of(r1, r2))); // maplenen response'lar listede olmalı
    }
}
