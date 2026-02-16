package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.SupportTicketRequest;
import com.mehmetkerem.dto.request.TicketReplyRequest;
import com.mehmetkerem.dto.response.SupportTicketResponse;

import java.util.List;

public interface ISupportTicketService {

    SupportTicketResponse create(Long userId, SupportTicketRequest request);

    List<SupportTicketResponse> getTicketsByUser(Long userId);

    SupportTicketResponse getTicketById(Long ticketId);

    SupportTicketResponse getTicketByIdForUser(Long ticketId, Long userId);

    List<SupportTicketResponse> getAllTickets();

    SupportTicketResponse addReply(Long ticketId, TicketReplyRequest request);
}
