package com.mehmetkerem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sipariş fişi/fatura bilgisi (yazdırma veya PDF için kullanılabilir).
 */
@Data
@Builder
public class OrderInvoiceResponse {

    private String invoiceNumber;
    private Long orderId;
    private LocalDateTime orderDate;
    private String customerName;
    private String shippingAddressSummary;
    private List<InvoiceItemLine> items;
    private BigDecimal subtotal;
    private BigDecimal totalAmount;
    private String orderStatus;

    @Data
    @Builder
    public static class InvoiceItemLine {
        private String productTitle;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}
