package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.ReviewRequest;
import com.mehmetkerem.dto.response.ReviewResponse;
import com.mehmetkerem.model.Review;

import java.util.List;

public interface IReviewService {

    ReviewResponse saveReview(Long userId, ReviewRequest request);

    String deleteReview(Long userId, Long id);

    ReviewResponse updateReview(Long userId, Long id, ReviewRequest request);

    ReviewResponse getReviewResponseById(Long id);

    Review getReviewById(Long id);

    List<ReviewResponse> findAllReviews();

    List<ReviewResponse> getReviewsByProductId(Long productId);
}
