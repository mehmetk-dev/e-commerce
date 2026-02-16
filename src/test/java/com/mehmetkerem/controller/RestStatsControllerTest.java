package com.mehmetkerem.controller;

import com.mehmetkerem.controller.impl.RestStatsControllerImpl;
import com.mehmetkerem.dto.response.StatsResponse;
import com.mehmetkerem.service.IStatsService;
import com.mehmetkerem.util.ResultData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestStatsControllerTest {

    @Mock
    private IStatsService statsService;

    @InjectMocks
    private RestStatsControllerImpl controller;

    @Test
    @DisplayName("getStats - ResultData ile istatistikler döner")
    void getStats_ShouldReturnSuccessWithStats() {
        StatsResponse stats = StatsResponse.builder()
                .totalRevenue(BigDecimal.TEN)
                .totalOrders(5L)
                .lowStockProducts(2L)
                .totalProducts(100L)
                .build();
        when(statsService.getAdminStats()).thenReturn(stats);

        ResultData<StatsResponse> result = controller.getStats();

        assertTrue(result.isStatus());
        assertNotNull(result.getData());
        assertEquals(BigDecimal.TEN, result.getData().getTotalRevenue());
        assertEquals(5L, result.getData().getTotalOrders());
        verify(statsService).getAdminStats();
    }
}
