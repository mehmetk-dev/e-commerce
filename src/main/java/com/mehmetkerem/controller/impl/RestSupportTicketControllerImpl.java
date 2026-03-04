package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestSupportTicketController;
import com.mehmetkerem.dto.request.SupportTicketRequest;
import com.mehmetkerem.dto.request.TicketReplyRequest;
import com.mehmetkerem.dto.response.SupportTicketResponse;
import com.mehmetkerem.service.ISupportTicketService;
import com.mehmetkerem.dto.response.CursorResponse;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import com.mehmetkerem.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/support")
@RequiredArgsConstructor
public class RestSupportTicketControllerImpl implements IRestSupportTicketController {

    private final ISupportTicketService supportTicketService;

    private static long requireCurrentUserId() {
        Long id = SecurityUtils.getCurrentUserId();
        if (id == null) {
            throw new InsufficientAuthenticationException("Oturum gerekli");
        }
        return id;
    }

    @Override
    @PostMapping
    public ResultData<SupportTicketResponse> create(@Valid @RequestBody SupportTicketRequest request) {
        return ResultHelper.success(supportTicketService.create(requireCurrentUserId(), request));
    }

    @Override
    @GetMapping("/my-tickets")
    public ResultData<List<SupportTicketResponse>> getMyTickets() {
        return ResultHelper.success(supportTicketService.getTicketsByUser(requireCurrentUserId()));
    }

    @Override
    @GetMapping("/my-tickets/{ticketId}")
    public ResultData<SupportTicketResponse> getMyTicketById(@PathVariable Long ticketId) {
        return ResultHelper.success(supportTicketService.getTicketByIdForUser(ticketId, requireCurrentUserId()));
    }

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<List<SupportTicketResponse>> getAllTickets() {
        return ResultHelper.success(supportTicketService.getAllTickets());
    }

    @GetMapping("/all-paged")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<CursorResponse<SupportTicketResponse>> getAllTicketsPaged(
            @RequestParam(required = false) com.mehmetkerem.enums.TicketStatus status,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResultHelper.cursor(supportTicketService.getAllTicketsForAdmin(status, pageable));
    }

    @DeleteMapping("/{ticketId}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<Void> deleteForAdmin(@PathVariable Long ticketId) {
        supportTicketService.deleteForAdmin(ticketId);
        return ResultHelper.success(null);
    }

    @Override
    @PutMapping("/{ticketId}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<SupportTicketResponse> addReply(@PathVariable Long ticketId,
            @Valid @RequestBody TicketReplyRequest request) {
        return ResultHelper.success(supportTicketService.addReply(ticketId, request));
    }
}
