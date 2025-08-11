package com.mehmetkerem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "reviews")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review {

    @Id
    private String id;

    @Field("product_id")
    private String productId;

    @Field("user_id")
    private String userId;

    private String comment;

    private int rating;

    @Field("created_at")
    private LocalDateTime createdAt;
}
