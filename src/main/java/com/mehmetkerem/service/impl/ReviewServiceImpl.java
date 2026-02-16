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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("null")
@Slf4j
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserServiceImpl userService;
    private final ProductServiceImpl productService;
    private final com.mehmetkerem.repository.OrderRepository orderRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ReviewMapper reviewMapper, UserServiceImpl userService,
            ProductServiceImpl productService, com.mehmetkerem.repository.OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.userService = userService;
        this.productService = productService;
        this.orderRepository = orderRepository;
    }

    @Transactional
    @Override
    public ReviewResponse saveReview(ReviewRequest request) {
        log.info("Yeni yorum isteği. Kullanıcı ID: {}, Ürün ID: {}", request.getUserId(), request.getProductId());

        // Verify purchase
        boolean hasPurchased = orderRepository.existsByUserIdAndOrderItemsProductId(request.getUserId(),
                request.getProductId());

        if (!hasPurchased) {
            log.warn("Yorum reddedildi: Ürün satın alınmamış. Kullanıcı ID: {}, Ürün ID: {}", request.getUserId(),
                    request.getProductId());
            throw new com.mehmetkerem.exception.BadRequestException(
                    "Bu ürünü satın almadığınız için yorum yapamazsınız.");
        }

        // Check if already reviewed
        boolean alreadyReviewed = reviewRepository.existsByUserIdAndProductId(request.getUserId(),
                request.getProductId());
        if (alreadyReviewed) {
            log.warn("Yorum reddedildi: Mükerrer yorum. Kullanıcı ID: {}, Ürün ID: {}", request.getUserId(),
                    request.getProductId());
            throw new com.mehmetkerem.exception.BadRequestException("Bu ürüne zaten yorum yapmışsınız.");
        }

        Review savedReview = reviewRepository.save(reviewMapper.toEntity(request));
        log.info("Yorum başarıyla kaydedildi. Yorum ID: {}", savedReview.getId());
        return getDetails(savedReview);
    }

    @Override
    public String deleteReview(Long id) {
        reviewRepository.delete(getReviewById(id));
        return String.format(Messages.DELETE_VALUE, id, "yorum");
    }

    @Transactional
    @Override
    public ReviewResponse updateReview(Long id, ReviewRequest request) {
        Review currentReview = getReviewById(id);
        reviewMapper.update(currentReview, request);
        return getDetails(reviewRepository.save(currentReview));
    }

    @Override
    public ReviewResponse getReviewResponseById(Long id) {
        return getDetails(getReviewById(id));
    }

    @Override
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(ExceptionMessages.NOT_FOUND, id, "yorum")));
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
