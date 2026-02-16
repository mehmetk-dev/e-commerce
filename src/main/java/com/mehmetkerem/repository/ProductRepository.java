package com.mehmetkerem.repository;

import com.mehmetkerem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByIdIn(List<Long> ids);

    List<Product> findByTitleContainingIgnoreCase(String title);

    List<Product> findByCategoryId(Long categoryId);

    long countByStockLessThan(int threshold);
}
