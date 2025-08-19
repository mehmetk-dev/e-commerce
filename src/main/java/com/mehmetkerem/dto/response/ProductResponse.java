package com.mehmetkerem.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductResponse {
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private CategoryResponse category;
    private List<String> imageUrls;
    private Map<String, Object> attributes;
}
