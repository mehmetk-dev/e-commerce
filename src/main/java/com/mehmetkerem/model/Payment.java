package com.mehmetkerem.model;

import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "payments")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {

    @Id
    private String id;

    @Field("order_id")
    private String orderId;

    @Field("user_id")
    private String userId;

    @Field("amount")
    private BigDecimal amount;

    @Field("payment_method")
    private PaymentMethod paymentMethod;

    @Field("payment_status")
    private PaymentStatus paymentStatus;

    @Field("transaction_id")
    private String transactionId;

    @Field("created_at")
    private LocalDateTime createdAt;
}
