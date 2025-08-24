package com.mehmetkerem.SpringMVCBackEnd.service;

import com.mehmetkerem.dto.request.ReviewRequest;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.dto.response.ReviewResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.ReviewMapper;
import com.mehmetkerem.model.Review;
import com.mehmetkerem.repository.ReviewRepository;
import com.mehmetkerem.service.impl.ProductServiceImpl;
import com.mehmetkerem.service.impl.ReviewServiceImpl;
import com.mehmetkerem.service.impl.UserServiceImpl;
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

class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private ProductServiceImpl productService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // mock'ları initialize et
    }

    // saveReview: review kaydedilir, kullanıcı/ürün detayları çekilir ve detaylı response döner
    @Test
    void saveReview_success() {
        ReviewRequest req = new ReviewRequest();
        Review entity = new Review();
        entity.setUserId("u1");
        entity.setProductId("p1");
        Review saved = new Review();
        saved.setUserId("u1");
        saved.setProductId("p1");

        UserResponse userResp = new UserResponse();
        userResp.setId("u1");
        ProductResponse prodResp = new ProductResponse();
        prodResp.setId("p1");
        ReviewResponse resp = new ReviewResponse();

        when(reviewMapper.toEntity(req)).thenReturn(entity);
        when(reviewRepository.save(entity)).thenReturn(saved);

        // getDetails zinciri
        when(userService.getUserResponseById("u1")).thenReturn(userResp);
        when(productService.getProductResponseById("p1")).thenReturn(prodResp);
        when(reviewMapper.toResponseWithDetails(saved, prodResp, userResp)).thenReturn(resp);

        ReviewResponse result = reviewService.saveReview(req);

        assertNotNull(result); // response null olmamalı
        verify(reviewRepository).save(entity); // kayıt yapılmalı
        verify(reviewMapper).toResponseWithDetails(saved, prodResp, userResp); // detaylı mapping yapılmalı
    }

    // deleteReview: mevcut review silinir ve mesaj döner
    @Test
    void deleteReview_success() {
        String id = "r1";
        Review review = new Review();
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));

        String result = reviewService.deleteReview(id);

        String expected = String.format(Messages.DELETE_VALUE, id, "yorum");
        assertEquals(expected, result); // mesaj formatı beklenen gibi
        verify(reviewRepository).delete(review); // delete çağrılmalı
    }

    // deleteReview: review yoksa NotFoundException
    @Test
    void deleteReview_notFound() {
        when(reviewRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reviewService.deleteReview("missing")); // bulunamayan id
        verify(reviewRepository, never()).delete(any());
    }

    // updateReview: review güncellenir, kaydedilir ve detaylı response döner
    @Test
    void updateReview_success() {
        String id = "r1";
        ReviewRequest req = new ReviewRequest();

        Review current = new Review();
        current.setUserId("u1");
        current.setProductId("p1");

        Review saved = new Review();
        saved.setUserId("u1");
        saved.setProductId("p1");

        UserResponse userResp = new UserResponse();
        userResp.setId("u1");
        ProductResponse prodResp = new ProductResponse();
        prodResp.setId("p1");
        ReviewResponse resp = new ReviewResponse();

        when(reviewRepository.findById(id)).thenReturn(Optional.of(current));
        doNothing().when(reviewMapper).update(current, req);
        when(reviewRepository.save(current)).thenReturn(saved);

        when(userService.getUserResponseById("u1")).thenReturn(userResp);
        when(productService.getProductResponseById("p1")).thenReturn(prodResp);
        when(reviewMapper.toResponseWithDetails(saved, prodResp, userResp)).thenReturn(resp);

        ReviewResponse result = reviewService.updateReview(id, req);

        assertNotNull(result); // response null olmamalı
        verify(reviewMapper).update(current, req); // update mapper çağrılmalı
        verify(reviewRepository).save(current); // save edilmeli
    }

    // getReviewResponseById: review bulunur, detaylar çekilir ve detaylı response döner
    @Test
    void getReviewResponseById_success() {
        String id = "r1";
        Review review = new Review();
        review.setUserId("u1");
        review.setProductId("p1");

        UserResponse userResp = new UserResponse();
        userResp.setId("u1");
        ProductResponse prodResp = new ProductResponse();
        prodResp.setId("p1");
        ReviewResponse resp = new ReviewResponse();

        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));
        when(userService.getUserResponseById("u1")).thenReturn(userResp);
        when(productService.getProductResponseById("p1")).thenReturn(prodResp);
        when(reviewMapper.toResponseWithDetails(review, prodResp, userResp)).thenReturn(resp);

        ReviewResponse result = reviewService.getReviewResponseById(id);

        assertNotNull(result); // detaylı response dönmeli
    }

    // getReviewById: review varsa entity dönmeli
    @Test
    void getReviewById_success() {
        String id = "r1";
        Review review = new Review();
        review.setId(id);
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));

        Review result = reviewService.getReviewById(id);

        assertEquals(id, result.getId()); // id eşleşmeli
    }

    // getReviewById: review yoksa NotFoundException
    @Test
    void getReviewById_notFound() {
        when(reviewRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reviewService.getReviewById("missing")); // bulunamayan id
    }

    // findAllReviews: tüm review'lar detaylarıyla response listesine maplenmeli
    @Test
    void findAllReviews_success() {
        Review r1 = new Review();
        r1.setUserId("u1");
        r1.setProductId("p1");
        Review r2 = new Review();
        r2.setUserId("u2");
        r2.setProductId("p2");

        UserResponse u1 = new UserResponse();
        u1.setId("u1");
        UserResponse u2 = new UserResponse();
        u2.setId("u2");
        ProductResponse p1 = new ProductResponse();
        p1.setId("p1");
        ProductResponse p2 = new ProductResponse();
        p2.setId("p2");

        ReviewResponse rr1 = new ReviewResponse();
        ReviewResponse rr2 = new ReviewResponse();

        when(reviewRepository.findAll()).thenReturn(List.of(r1, r2));

        // getDetails(r1)
        when(userService.getUserResponseById("u1")).thenReturn(u1);
        when(productService.getProductResponseById("p1")).thenReturn(p1);
        when(reviewMapper.toResponseWithDetails(r1, p1, u1)).thenReturn(rr1);

        // getDetails(r2)
        when(userService.getUserResponseById("u2")).thenReturn(u2);
        when(productService.getProductResponseById("p2")).thenReturn(p2);
        when(reviewMapper.toResponseWithDetails(r2, p2, u2)).thenReturn(rr2);

        List<ReviewResponse> results = reviewService.findAllReviews();

        assertEquals(2, results.size()); // iki review dönmeli
        assertTrue(results.containsAll(List.of(rr1, rr2))); // maplenen response'lar listede olmalı
    }
}
