package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.SupportTicketRequest;
import com.mehmetkerem.dto.request.TicketReplyRequest;
import com.mehmetkerem.dto.response.SupportTicketResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.model.SupportTicket;
import com.mehmetkerem.repository.SupportTicketRepository;
import com.mehmetkerem.service.ISupportTicketService;
import com.mehmetkerem.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class SupportTicketServiceImpl implements ISupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final IUserService userService;

    @Override
    @Transactional
    public SupportTicketResponse create(Long userId, SupportTicketRequest request) {
        SupportTicket ticket = SupportTicket.builder()
                .userId(userId)
                .subject(request.getSubject())
                .message(request.getMessage())
                .build();
        ticket = supportTicketRepository.save(ticket);
        return toResponse(ticket);
    }

    @Override
    public List<SupportTicketResponse> getTicketsByUser(Long userId) {
        return supportTicketRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SupportTicketResponse getTicketById(Long ticketId) {
        return toResponse(getById(ticketId));
    }

    @Override
    public SupportTicketResponse getTicketByIdForUser(Long ticketId, Long userId) {
        SupportTicket ticket = getById(ticketId);
        if (!ticket.getUserId().equals(userId)) {
            throw new BadRequestException("Bu destek talebi size ait değil.");
        }
        return toResponse(ticket);
    }

    @Override
    public List<SupportTicketResponse> getAllTickets() {
        return supportTicketRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SupportTicketResponse addReply(Long ticketId, TicketReplyRequest request) {
        SupportTicket ticket = getById(ticketId);
        ticket.setStatus(request.getStatus());
        if (request.getAdminReply() != null && !request.getAdminReply().isBlank()) {
            ticket.setAdminReply(request.getAdminReply());
        }
        ticket = supportTicketRepository.save(ticket);
        return toResponse(ticket);
    }

    private SupportTicket getById(Long id) {
        return supportTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(ExceptionMessages.NOT_FOUND, id, "destek talebi")));
    }

    private SupportTicketResponse toResponse(SupportTicket t) {
        UserResponse user = userService.getUserResponseById(t.getUserId());
        return SupportTicketResponse.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .userEmail(user.getEmail())
                .userName(user.getName())
                .subject(t.getSubject())
                .message(t.getMessage())
                .status(t.getStatus())
                .adminReply(t.getAdminReply())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
