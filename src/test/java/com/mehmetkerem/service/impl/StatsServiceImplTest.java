package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.response.StatsResponse;
import com.mehmetkerem.repository.OrderRepository;
import com.mehmetkerem.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("getAdminStats - tüm istatistikler doğru toplanır")
    void getAdminStats_ShouldReturnAggregatedStats() {
        when(orderRepository.calculateTotalRevenue()).thenReturn(new BigDecimal("15000.50"));
        when(orderRepository.countTotalOrders()).thenReturn(42L);
        when(orderRepository.getDailyStats(any(LocalDateTime.class)))
                .thenReturn(List.<Object[]>of(
                        new Object[] { "2025-02-10", new BigDecimal("1000"), 5L },
                        new Object[] { "2025-02-11", new BigDecimal("2000"), 8L }));
        when(productRepository.countByStockLessThan(5)).thenReturn(3L);
        when(productRepository.count()).thenReturn(100L);

        StatsResponse result = statsService.getAdminStats();

        assertNotNull(result);
        assertEquals(new BigDecimal("15000.50"), result.getTotalRevenue());
        assertEquals(42L, result.getTotalOrders());
        assertEquals(3L, result.getLowStockProducts());
        assertEquals(100L, result.getTotalProducts());
        assertNotNull(result.getDailyStats());
        assertEquals(2, result.getDailyStats().size());
        assertEquals("2025-02-10", result.getDailyStats().get(0).getDate());
        assertEquals(new BigDecimal("1000"), result.getDailyStats().get(0).getRevenue());
        assertEquals(5L, result.getDailyStats().get(0).getOrderCount());
    }

    @Test
    @DisplayName("getAdminStats - revenue null ise ZERO kullanılır")
    void getAdminStats_WhenRevenueNull_ShouldUseZero() {
        when(orderRepository.calculateTotalRevenue()).thenReturn(null);
        when(orderRepository.countTotalOrders()).thenReturn(0L);
        when(orderRepository.getDailyStats(any(LocalDateTime.class))).thenReturn(List.of());
        when(productRepository.countByStockLessThan(5)).thenReturn(0L);
        when(productRepository.count()).thenReturn(0L);

        StatsResponse result = statsService.getAdminStats();

        assertEquals(BigDecimal.ZERO, result.getTotalRevenue());
    }

    @Test
    @DisplayName("getAdminStats - günlük satırda revenue null ise ZERO")
    void getAdminStats_WhenDailyRevenueNull_ShouldUseZero() {
        when(orderRepository.calculateTotalRevenue()).thenReturn(BigDecimal.ZERO);
        when(orderRepository.countTotalOrders()).thenReturn(0L);
        Object[] row = new Object[] { "2025-02-10", null, 2L };
        when(orderRepository.getDailyStats(any(LocalDateTime.class)))
                .thenReturn(List.<Object[]>of(row));
        when(productRepository.countByStockLessThan(5)).thenReturn(0L);
        when(productRepository.count()).thenReturn(0L);

        StatsResponse result = statsService.getAdminStats();

        assertEquals(1, result.getDailyStats().size());
        assertEquals(BigDecimal.ZERO, result.getDailyStats().get(0).getRevenue());
        assertEquals(2L, result.getDailyStats().get(0).getOrderCount());
    }
}
