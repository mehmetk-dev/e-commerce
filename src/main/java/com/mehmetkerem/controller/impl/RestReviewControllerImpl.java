package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestReviewController;
import com.mehmetkerem.dto.request.ReviewRequest;
import com.mehmetkerem.dto.response.ReviewResponse;
import com.mehmetkerem.service.IReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ReviewResponse> saveReview(@Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.saveReview(request));
    }

    @GetMapping("/find-all")
    @Override
    public ResponseEntity<List<ReviewResponse>> findAllReviews() {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.findAllReviews());
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable("id") String id, @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.updateReview(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<String> deleteReview(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.deleteReview(id));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.FOUND).body(reviewService.getReviewResponseById(id));
    }
}
