package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.OrderReturnRequest;
import com.mehmetkerem.dto.response.OrderReturnResponse;

import java.util.List;

public interface IOrderReturnService {

    OrderReturnResponse createReturn(Long userId, OrderReturnRequest request);

    List<OrderReturnResponse> getReturnsByUser(Long userId);

    List<OrderReturnResponse> getAllReturns();

    OrderReturnResponse approve(Long returnId);

    OrderReturnResponse reject(Long returnId);
}
