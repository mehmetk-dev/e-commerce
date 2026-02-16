package com.mehmetkerem.controller;

import com.mehmetkerem.dto.response.CursorResponse;
import com.mehmetkerem.dto.response.OrderInvoiceResponse;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.util.ResultData;

public interface IRestOrderController {
    ResultData<OrderResponse> updateTrackingInfo(Long orderId, String trackingNumber, String carrierName);

    ResultData<CursorResponse<OrderResponse>> getAllOrders(int page, int size, String sortBy, String direction);

    ResultData<CursorResponse<OrderResponse>> getMyOrders(int page, int size, String sortBy, String direction);

    ResultData<OrderResponse> saveOrder(com.mehmetkerem.dto.request.OrderRequest request);

    /** Kendi siparişi veya admin için sipariş fişi. */
    ResultData<OrderInvoiceResponse> getOrderInvoice(Long orderId);
}
