package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.ReviewRequest;
import com.mehmetkerem.dto.response.ReviewResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IRestReviewController {

    ResponseEntity<ReviewResponse> saveReview(ReviewRequest request);

    ResponseEntity<List<ReviewResponse>> findAllReviews();

    ResponseEntity<ReviewResponse> updateReview(String id, ReviewRequest request);

    ResponseEntity<String> deleteReview(String id);

    ResponseEntity<ReviewResponse> getReviewById(String id);
}
