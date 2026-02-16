package com.mehmetkerem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StatsResponse {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long lowStockProducts;
    private long totalProducts;
    private java.util.List<DailyStats> dailyStats;

    @Data
    @Builder
    public static class DailyStats {
        private String date;
        private BigDecimal revenue;
        private long orderCount;
    }
}
