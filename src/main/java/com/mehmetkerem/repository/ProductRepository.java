package com.mehmetkerem.repository;

import com.mehmetkerem.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByIdIn(List<String> ids);

    List<Product> findByTitleContainingIgnoreCase(String title);

    List<Product> findByCategoryId(String categoryId);
}
