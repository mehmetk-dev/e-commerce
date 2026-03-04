package com.mehmetkerem.controller;

import com.mehmetkerem.controller.impl.RestReviewControllerImpl;
import com.mehmetkerem.dto.request.ReviewRequest;
import com.mehmetkerem.dto.response.ReviewResponse;
import com.mehmetkerem.service.IReviewService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class RestReviewControllerTest {

    @Mock
    private IReviewService reviewService;

    @InjectMocks
    private RestReviewControllerImpl controller;

    private ReviewRequest reviewRequest;
    private ReviewResponse reviewResponse;
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        reviewRequest = new ReviewRequest();
        reviewRequest.setProductId(10L);
        reviewRequest.setComment("Güzel");
        reviewRequest.setRating(5.0);
        reviewResponse = new ReviewResponse();
        reviewResponse.setId(1L);
        reviewResponse.setComment("Güzel");
    }

    @Test
    @DisplayName("saveReview - 201 ve yorum döner")
    void saveReview_ShouldReturn201() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);
            when(reviewService.saveReview(eq(TEST_USER_ID), any(ReviewRequest.class))).thenReturn(reviewResponse);

            ResultData<ReviewResponse> response = controller.saveReview(reviewRequest);

            assertTrue(response.isStatus());
            assertEquals(1L, response.getData().getId());
            verify(reviewService).saveReview(TEST_USER_ID, reviewRequest);
        }
    }

    @Test
    @DisplayName("findAllReviews - liste döner")
    void findAllReviews_ShouldReturnList() {
        when(reviewService.findAllReviews()).thenReturn(List.of(reviewResponse));

        ResultData<List<ReviewResponse>> response = controller.findAllReviews();

        assertTrue(response.isStatus());
        assertEquals(1, response.getData().size());
        verify(reviewService).findAllReviews();
    }

    @Test
    @DisplayName("getReviewById - yorum döner")
    void getReviewById_ShouldReturnReview() {
        when(reviewService.getReviewResponseById(1L)).thenReturn(reviewResponse);

        ResultData<ReviewResponse> response = controller.getReviewById(1L);

        assertTrue(response.isStatus());
        assertEquals(1L, response.getData().getId());
        verify(reviewService).getReviewResponseById(1L);
    }

    @Test
    @DisplayName("updateReview - güncel yorum döner")
    void updateReview_ShouldReturnUpdated() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);
            when(reviewService.updateReview(TEST_USER_ID, 1L, reviewRequest)).thenReturn(reviewResponse);

            ResultData<ReviewResponse> response = controller.updateReview(1L, reviewRequest);

            assertTrue(response.isStatus());
            verify(reviewService).updateReview(TEST_USER_ID, 1L, reviewRequest);
        }
    }

    @Test
    @DisplayName("deleteReview - mesaj döner")
    void deleteReview_ShouldReturnMessage() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);
            when(reviewService.deleteReview(TEST_USER_ID, 1L)).thenReturn("1 ID'li yorum silinmiştir!");

            ResultData<String> response = controller.deleteReview(1L);

            assertTrue(response.isStatus());
            assertTrue(response.getData().contains("1"));
            verify(reviewService).deleteReview(TEST_USER_ID, 1L);
        }
    }
}
