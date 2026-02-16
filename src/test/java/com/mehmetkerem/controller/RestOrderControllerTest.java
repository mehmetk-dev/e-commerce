package com.mehmetkerem.controller;

import com.mehmetkerem.controller.impl.RestOrderControllerImpl;
import com.mehmetkerem.dto.request.OrderRequest;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.service.IOrderService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class RestOrderControllerTest {

    @Mock
    private IOrderService orderService;

    @InjectMocks
    private RestOrderControllerImpl controller;

    private OrderRequest orderRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        SecurityTestUtils.setCurrentUser();
        orderRequest = new OrderRequest();
        orderRequest.setAddressId(1L);
        orderResponse = OrderResponse.builder().id(1L).build();
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clearContext();
    }

    @Test
    @DisplayName("saveOrder - ResultData success ve sipariş döner")
    void saveOrder_ShouldReturnSuccessResultData() {
        when(orderService.saveOrder(eq(SecurityTestUtils.DEFAULT_USER_ID), any(OrderRequest.class))).thenReturn(orderResponse);

        ResultData<OrderResponse> result = controller.saveOrder(orderRequest);

        assertTrue(result.isStatus());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
        verify(orderService).saveOrder(eq(SecurityTestUtils.DEFAULT_USER_ID), any(OrderRequest.class));
    }

    @Test
    @DisplayName("getAllOrders - sayfalı liste döner")
    void getAllOrders_ShouldReturnPagedList() {
        when(orderService.getAllOrders(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(orderResponse)));

        ResultData<com.mehmetkerem.dto.response.CursorResponse<OrderResponse>> result =
                controller.getAllOrders(0, 20, "orderDate", "desc");

        assertTrue(result.isStatus());
        assertNotNull(result.getData().getItems());
        assertEquals(1, result.getData().getItems().size());
        verify(orderService).getAllOrders(any(Pageable.class));
    }

    @Test
    @DisplayName("getMyOrders - kullanıcının siparişleri sayfalı döner")
    void getMyOrders_ShouldReturnUserOrdersPaged() {
        when(orderService.getOrdersByUser(eq(SecurityTestUtils.DEFAULT_USER_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(orderResponse)));

        ResultData<com.mehmetkerem.dto.response.CursorResponse<OrderResponse>> result =
                controller.getMyOrders(0, 20, "orderDate", "desc");

        assertTrue(result.isStatus());
        assertEquals(1, result.getData().getItems().size());
        verify(orderService).getOrdersByUser(eq(SecurityTestUtils.DEFAULT_USER_ID), any(Pageable.class));
    }

    @Test
    @DisplayName("updateTrackingInfo - kargo bilgisi güncellenir")
    void updateTrackingInfo_ShouldReturnUpdatedOrder() {
        orderResponse.setTrackingNumber("TRK123");
        orderResponse.setCarrierName("Kargo A");
        when(orderService.updateOrderTracking(1L, "TRK123", "Kargo A")).thenReturn(orderResponse);

        ResultData<OrderResponse> result = controller.updateTrackingInfo(1L, "TRK123", "Kargo A");

        assertTrue(result.isStatus());
        assertEquals("TRK123", result.getData().getTrackingNumber());
        verify(orderService).updateOrderTracking(1L, "TRK123", "Kargo A");
    }
}
