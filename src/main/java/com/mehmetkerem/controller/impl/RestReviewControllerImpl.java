package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestReviewController;
import com.mehmetkerem.dto.request.ReviewRequest;
import com.mehmetkerem.dto.response.ReviewResponse;
import com.mehmetkerem.service.IReviewService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/review")
public class RestReviewControllerImpl implements IRestReviewController {

    private final IReviewService reviewService;

    public RestReviewControllerImpl(IReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/save")
    @Override
    public ResultData<ReviewResponse> saveReview(@Valid @RequestBody ReviewRequest request) {
        return ResultHelper.success(reviewService.saveReview(request));
    }

    @GetMapping("/find-all")
    @Override
    public ResultData<List<ReviewResponse>> findAllReviews() {
        return ResultHelper.success(reviewService.findAllReviews());
    }

    @PutMapping("/{id}")
    @Override
    public ResultData<ReviewResponse> updateReview(@PathVariable("id") Long id,
            @RequestBody ReviewRequest request) {
        return ResultHelper.success(reviewService.updateReview(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResultData<String> deleteReview(@PathVariable("id") Long id) {
        return ResultHelper.success(reviewService.deleteReview(id));
    }

    @GetMapping("/{id}")
    @Override
    public ResultData<ReviewResponse> getReviewById(@PathVariable("id") Long id) {
        return ResultHelper.success(reviewService.getReviewResponseById(id));
    }
}
