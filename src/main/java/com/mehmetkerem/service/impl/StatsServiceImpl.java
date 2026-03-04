package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.response.StatsResponse;
import com.mehmetkerem.repository.OrderRepository;
import com.mehmetkerem.repository.ProductRepository;
import com.mehmetkerem.service.IStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements IStatsService {

        private final OrderRepository orderRepository;
        private final ProductRepository productRepository;

        @Value("${app.stats.daily-range-days:7}")
        private int dailyRangeDays;

        @Value("${app.stock.alert-threshold:5}")
        private int stockAlertThreshold;

        @Override
        public StatsResponse getAdminStats() {
                BigDecimal revenue = orderRepository.calculateTotalRevenue();

                java.time.LocalDateTime rangeStart = java.time.LocalDateTime.now().minusDays(dailyRangeDays);
                java.util.List<Object[]> rawStats = orderRepository.getDailyStats(rangeStart);

                java.util.List<StatsResponse.DailyStats> dailyStats = rawStats.stream()
                                .map(row -> StatsResponse.DailyStats.builder()
                                                .date((String) row[0])
                                                .revenue(row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO)
                                                .orderCount(((Number) row[2]).longValue())
                                                .build())
                                .toList();

                return StatsResponse.builder()
                                .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                                .totalOrders(orderRepository.countTotalOrders())
                                .lowStockProducts(productRepository.countByStockLessThan(stockAlertThreshold))
                                .totalProducts(productRepository.count())
                                .dailyStats(dailyStats)
                                .build();
        }
}
