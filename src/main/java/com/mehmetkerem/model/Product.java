package com.mehmetkerem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "products")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    @Id
    private String id;

    private String title;

    private String description;

    private BigDecimal price;

    private int quantity;

    @Field("category_id")
    private String categoryId;

    @Field("image_urls")
    private List<String> imageUrls;

    private Map<String, Object> attributes;
}
