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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final com.mehmetkerem.service.IUserService userService;
    private final com.mehmetkerem.service.IProductService productService;
    private final com.mehmetkerem.repository.OrderRepository orderRepository;

    @Transactional
    @Override
    public ReviewResponse saveReview(Long userId, ReviewRequest request) {
        log.info("Yeni yorum isteği. Kullanıcı ID: {}, Ürün ID: {}", userId, request.getProductId());

        // Verify purchase
        boolean hasPurchased = orderRepository.existsByUserIdAndOrderItemsProductId(userId,
                request.getProductId());

        if (!hasPurchased) {
            log.warn("Yorum reddedildi: Ürün satın alınmamış. Kullanıcı ID: {}, Ürün ID: {}", userId,
                    request.getProductId());
            throw new com.mehmetkerem.exception.BadRequestException(
                    "Bu ürünü satın almadığınız için yorum yapamazsınız.");
        }

        // Check if already reviewed
        boolean alreadyReviewed = reviewRepository.existsByUserIdAndProductId(userId,
                request.getProductId());
        if (alreadyReviewed) {
            log.warn("Yorum reddedildi: Mükerrer yorum. Kullanıcı ID: {}, Ürün ID: {}", userId,
                    request.getProductId());
            throw new com.mehmetkerem.exception.BadRequestException("Bu ürüne zaten yorum yapmışsınız.");
        }

        Review review = reviewMapper.toEntity(request);
        review.setUserId(userId);
        Review savedReview = reviewRepository.save(review);
        log.info("Yorum başarıyla kaydedildi. Yorum ID: {}", savedReview.getId());

        // Recalculate product rating
        recalculateProductRating(request.getProductId());

        return getDetails(savedReview);
    }

    private void recalculateProductRating(Long productId) {
        double avgRating = reviewRepository.averageRatingByProductId(productId);
        int count = reviewRepository.countByProductId(productId);
        productService.updateProductRating(productId, avgRating, count);
        log.debug("Ürün rating güncellendi. Ürün ID: {}, Ortalama: {}, Yorum Sayısı: {}",
                productId, avgRating, count);
    }

    @Override
    public String deleteReview(Long userId, Long id) {
        Review review = getReviewById(id);
        if (!review.getUserId().equals(userId)) {
            throw new com.mehmetkerem.exception.BadRequestException("Bu yorumu silme yetkiniz yok.");
        }
        Long productId = review.getProductId();
        reviewRepository.delete(review);
        recalculateProductRating(productId);
        return String.format(Messages.DELETE_VALUE, id, "yorum");
    }

    @Transactional
    @Override
    public ReviewResponse updateReview(Long userId, Long id, ReviewRequest request) {
        Review currentReview = getReviewById(id);
        if (!currentReview.getUserId().equals(userId)) {
            throw new com.mehmetkerem.exception.BadRequestException("Bu yorumu güncelleme yetkiniz yok.");
        }
        reviewMapper.update(currentReview, request);
        ReviewResponse response = getDetails(reviewRepository.save(currentReview));
        recalculateProductRating(currentReview.getProductId());
        return response;
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

    @Override
    public List<ReviewResponse> getReviewsByProductId(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        if (reviews.isEmpty()) {
            return List.of();
        }

        // Ürün bilgisi tek sorgu — hepsi aynı product
        ProductResponse productResponse = productService.getProductResponseById(productId);

        // Kullanıcı bilgilerini batch çek
        List<Long> userIds = reviews.stream()
                .map(Review::getUserId)
                .distinct()
                .toList();

        // UserService'den batch almak mümkün değilse, en azından cache'li çağrılar
        // yapılır
        java.util.Map<Long, UserResponse> userMap = new java.util.HashMap<>();
        for (Long uid : userIds) {
            userMap.put(uid, userService.getUserResponseById(uid));
        }

        return reviews.stream()
                .map(review -> reviewMapper.toResponseWithDetails(
                        review, productResponse, userMap.get(review.getUserId())))
                .toList();
    }

    private ReviewResponse getDetails(Review review) {
        UserResponse userResponse = userService.getUserResponseById(review.getUserId());
        ProductResponse productResponse = productService.getProductResponseById(review.getProductId());
        return reviewMapper.toResponseWithDetails(review, productResponse, userResponse);
    }
}
