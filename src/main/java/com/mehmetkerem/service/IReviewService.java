package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.ReviewRequest;
import com.mehmetkerem.dto.response.ReviewResponse;
import com.mehmetkerem.model.Review;

import java.util.List;

public interface IReviewService {

    ReviewResponse saveReview(ReviewRequest request);

    String deleteReview(String id);

    ReviewResponse updateReview(String id, ReviewRequest request);

    ReviewResponse getReviewResponseById(String id);

    Review getReviewById(String id);

    List<ReviewResponse> findAllReviews();
}
