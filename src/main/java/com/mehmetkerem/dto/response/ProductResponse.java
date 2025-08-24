package com.mehmetkerem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
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

    public ProductResponse(String id, String title, BigDecimal price){
        this.id = id;
        this.title = title;
        this.price = price;
    }
}
