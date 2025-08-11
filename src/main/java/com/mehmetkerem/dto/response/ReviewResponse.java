package com.mehmetkerem.dto.response;

import com.mehmetkerem.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private String id;
    private ProductResponse product;
    private User user;
    private String comment;
    private int rating;
    private LocalDateTime createdAt;
}
