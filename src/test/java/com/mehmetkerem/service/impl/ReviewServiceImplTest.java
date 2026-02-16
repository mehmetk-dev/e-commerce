package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.ReviewRequest;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.dto.response.ReviewResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.ReviewMapper;
import com.mehmetkerem.model.Review;
import com.mehmetkerem.repository.OrderRepository;
import com.mehmetkerem.repository.ReviewRepository;
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
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private ProductServiceImpl productService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;
    private ReviewRequest reviewRequest;
    private Review review;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        reviewRequest = new ReviewRequest();
        reviewRequest.setUserId(USER_ID);
        reviewRequest.setProductId(PRODUCT_ID);
        reviewRequest.setComment("Çok güzel ürün");
        reviewRequest.setRating(5.0);

        review = Review.builder()
                .id(1L)
                .userId(USER_ID)
                .productId(PRODUCT_ID)
                .comment("Çok güzel ürün")
                .rating(5)
                .build();

        reviewResponse = new ReviewResponse();
        reviewResponse.setId(1L);
        reviewResponse.setComment("Çok güzel ürün");
        reviewResponse.setRating(5);
    }

    @Test
    @DisplayName("saveReview - ürün satın alınmamışsa BadRequestException")
    void saveReview_WhenNotPurchased_ShouldThrowBadRequestException() {
        when(orderRepository.existsByUserIdAndOrderItemsProductId(USER_ID, PRODUCT_ID)).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reviewService.saveReview(reviewRequest));

        assertTrue(ex.getMessage().contains("satın almadığınız"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveReview - zaten yorum yapılmışsa BadRequestException")
    void saveReview_WhenAlreadyReviewed_ShouldThrowBadRequestException() {
        when(orderRepository.existsByUserIdAndOrderItemsProductId(USER_ID, PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reviewService.saveReview(reviewRequest));

        assertTrue(ex.getMessage().contains("zaten yorum"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveReview - geçerli istekte yorum kaydedilir")
    void saveReview_WhenValid_ShouldSaveAndReturnResponse() {
        when(orderRepository.existsByUserIdAndOrderItemsProductId(USER_ID, PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(false);
        when(reviewMapper.toEntity(reviewRequest)).thenReturn(review);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(userService.getUserResponseById(USER_ID)).thenReturn(new UserResponse());
        when(productService.getProductResponseById(PRODUCT_ID)).thenReturn(new ProductResponse());
        when(reviewMapper.toResponseWithDetails(eq(review), any(), any())).thenReturn(reviewResponse);

        ReviewResponse result = reviewService.saveReview(reviewRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("getReviewById - yorum bulunamazsa NotFoundException")
    void getReviewById_WhenNotExists_ShouldThrowNotFoundException() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reviewService.getReviewById(999L));
    }

    @Test
    @DisplayName("getReviewResponseById - response döner")
    void getReviewResponseById_ShouldReturnResponse() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userService.getUserResponseById(USER_ID)).thenReturn(new UserResponse());
        when(productService.getProductResponseById(PRODUCT_ID)).thenReturn(new ProductResponse());
        when(reviewMapper.toResponseWithDetails(eq(review), any(), any())).thenReturn(reviewResponse);

        ReviewResponse result = reviewService.getReviewResponseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("updateReview - yorum güncellenir")
    void updateReview_WhenExists_ShouldUpdateAndReturn() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(userService.getUserResponseById(USER_ID)).thenReturn(new UserResponse());
        when(productService.getProductResponseById(PRODUCT_ID)).thenReturn(new ProductResponse());
        when(reviewMapper.toResponseWithDetails(eq(review), any(), any())).thenReturn(reviewResponse);

        ReviewResponse result = reviewService.updateReview(1L, reviewRequest);

        assertNotNull(result);
        verify(reviewMapper).update(eq(review), eq(reviewRequest));
        verify(reviewRepository).save(review);
    }

    @Test
    @DisplayName("deleteReview - yorum silinir")
    void deleteReview_WhenExists_ShouldDeleteAndReturnMessage() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        String result = reviewService.deleteReview(1L);

        assertTrue(result.contains("1"));
        assertTrue(result.contains("yorum"));
        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("findAllReviews - tüm yorumlar listelenir")
    void findAllReviews_ShouldReturnAllReviews() {
        when(reviewRepository.findAll()).thenReturn(List.of(review));
        when(userService.getUserResponseById(USER_ID)).thenReturn(new UserResponse());
        when(productService.getProductResponseById(PRODUCT_ID)).thenReturn(new ProductResponse());
        when(reviewMapper.toResponseWithDetails(eq(review), any(), any())).thenReturn(reviewResponse);

        List<ReviewResponse> result = reviewService.findAllReviews();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
