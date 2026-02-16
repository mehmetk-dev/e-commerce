package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.SupportTicketRequest;
import com.mehmetkerem.dto.request.TicketReplyRequest;
import com.mehmetkerem.dto.response.SupportTicketResponse;
import com.mehmetkerem.util.ResultData;

import java.util.List;

public interface IRestSupportTicketController {

    ResultData<SupportTicketResponse> create(SupportTicketRequest request);

    ResultData<List<SupportTicketResponse>> getMyTickets();

    ResultData<SupportTicketResponse> getMyTicketById(Long ticketId);

    ResultData<List<SupportTicketResponse>> getAllTickets();

    ResultData<SupportTicketResponse> addReply(Long ticketId, TicketReplyRequest request);
}
