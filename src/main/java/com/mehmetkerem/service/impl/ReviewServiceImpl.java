package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.ReviewRequest;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.dto.response.ReviewResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.ReviewMapper;
import com.mehmetkerem.model.Review;
import com.mehmetkerem.repository.ReviewRepository;
import com.mehmetkerem.service.IReviewService;
import com.mehmetkerem.util.Messages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserServiceImpl userService;
    private final ProductServiceImpl productService;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ReviewMapper reviewMapper, UserServiceImpl userService, ProductServiceImpl productService) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.userService = userService;
        this.productService = productService;
    }

    @Transactional
    @Override
    public ReviewResponse saveReview(ReviewRequest request) {
        return getDetails(reviewRepository.save(reviewMapper.toEntity(request)));
    }

    @Override
    public String deleteReview(String id) {
        reviewRepository.delete(getReviewById(id));
        return String.format(Messages.DELETE_VALUE, id, "yorum");
    }

    @Transactional
    @Override
    public ReviewResponse updateReview(String id, ReviewRequest request) {
        Review currentReview = getReviewById(id);
        reviewMapper.update(currentReview, request);
        return getDetails(reviewRepository.save(currentReview));
    }

    @Override
    public ReviewResponse getReviewResponseById(String id) {
        return getDetails(getReviewById(id));
    }

    @Override
    public Review getReviewById(String id) {
        return reviewRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format(ExceptionMessages.NOT_FOUND, id, "yorum")));
    }

    @Override
    public List<ReviewResponse> findAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::getDetails)
                .toList();
    }

    private ReviewResponse getDetails(Review review) {
        UserResponse userResponse = userService.getUserResponseById(review.getUserId());
        ProductResponse productResponse = productService.getProductResponseById(review.getProductId());
        return reviewMapper.toResponseWithDetails(review, productResponse, userResponse);
    }
}
