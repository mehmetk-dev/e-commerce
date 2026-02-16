package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.ReviewRequest;
import com.mehmetkerem.dto.response.ReviewResponse;
import com.mehmetkerem.model.Review;

import java.util.List;

public interface IReviewService {

    ReviewResponse saveReview(ReviewRequest request);

    String deleteReview(Long id);

    ReviewResponse updateReview(Long id, ReviewRequest request);

    ReviewResponse getReviewResponseById(Long id);

    Review getReviewById(Long id);

    List<ReviewResponse> findAllReviews();
}
