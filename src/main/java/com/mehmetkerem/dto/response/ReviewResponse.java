package com.mehmetkerem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {
    private Long id;
    private ProductResponse product;
    private UserResponse user;
    private String comment;
    private int rating;
    private LocalDateTime createdAt;
}
