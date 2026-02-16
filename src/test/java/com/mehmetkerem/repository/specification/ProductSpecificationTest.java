package com.mehmetkerem.repository.specification;

import com.mehmetkerem.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductSpecificationTest {

    @Test
    @DisplayName("hasTitle - null veya boş için conjunction döner")
    void hasTitle_NullOrEmpty_ReturnsNotNullSpecification() {
        Specification<Product> specNull = ProductSpecification.hasTitle(null);
        Specification<Product> specEmpty = ProductSpecification.hasTitle("");
        assertNotNull(specNull);
        assertNotNull(specEmpty);
    }

    @Test
    @DisplayName("hasTitle - metin için specification döner")
    void hasTitle_WithText_ReturnsNotNullSpecification() {
        Specification<Product> spec = ProductSpecification.hasTitle("Antika");
        assertNotNull(spec);
    }

    @Test
    @DisplayName("hasCategory - null için conjunction döner")
    void hasCategory_Null_ReturnsNotNullSpecification() {
        Specification<Product> spec = ProductSpecification.hasCategory(null);
        assertNotNull(spec);
    }

    @Test
    @DisplayName("hasCategory - id ile specification döner")
    void hasCategory_WithId_ReturnsNotNullSpecification() {
        Specification<Product> spec = ProductSpecification.hasCategory(1L);
        assertNotNull(spec);
    }

    @Test
    @DisplayName("priceBetween - her iki null için conjunction döner")
    void priceBetween_BothNull_ReturnsNotNullSpecification() {
        Specification<Product> spec = ProductSpecification.priceBetween(null, null);
        assertNotNull(spec);
    }

    @Test
    @DisplayName("priceBetween - min max ile specification döner")
    void priceBetween_WithMinMax_ReturnsNotNullSpecification() {
        Specification<Product> spec = ProductSpecification.priceBetween(
                new BigDecimal("10"), new BigDecimal("100"));
        assertNotNull(spec);
    }

    @Test
    @DisplayName("priceBetween - sadece min ile specification döner")
    void priceBetween_OnlyMin_ReturnsNotNullSpecification() {
        Specification<Product> spec = ProductSpecification.priceBetween(new BigDecimal("50"), null);
        assertNotNull(spec);
    }

    @Test
    @DisplayName("priceBetween - sadece max ile specification döner")
    void priceBetween_OnlyMax_ReturnsNotNullSpecification() {
        Specification<Product> spec = ProductSpecification.priceBetween(null, new BigDecimal("200"));
        assertNotNull(spec);
    }

    @Test
    @DisplayName("greaterThanRating - null için conjunction döner")
    void greaterThanRating_Null_ReturnsNotNullSpecification() {
        Specification<Product> spec = ProductSpecification.greaterThanRating(null);
        assertNotNull(spec);
    }

    @Test
    @DisplayName("greaterThanRating - değer ile specification döner")
    void greaterThanRating_WithValue_ReturnsNotNullSpecification() {
        Specification<Product> spec = ProductSpecification.greaterThanRating(4.0);
        assertNotNull(spec);
    }
}
