package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.ReviewRequest;
import com.mehmetkerem.dto.response.ReviewResponse;
import com.mehmetkerem.util.ResultData;

import java.util.List;

public interface IRestReviewController {

    ResultData<ReviewResponse> saveReview(ReviewRequest request);

    ResultData<List<ReviewResponse>> findAllReviews();

    ResultData<ReviewResponse> updateReview(Long id, ReviewRequest request);

    ResultData<String> deleteReview(Long id);

    ResultData<ReviewResponse> getReviewById(Long id);
}
