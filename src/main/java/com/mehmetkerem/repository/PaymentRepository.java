package com.mehmetkerem.repository;

import com.mehmetkerem.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment,String> {

    List<Payment> findByUserId(String userId);
    List<Payment> findByOrderId(String orderId);
}
