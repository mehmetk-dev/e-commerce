package com.mehmetkerem.repository;

import com.mehmetkerem.enums.TicketStatus;
import com.mehmetkerem.model.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

        List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);

        Page<SupportTicket> findByDeletedByAdminFalse(Pageable pageable);

        Page<SupportTicket> findByDeletedByAdminFalseAndStatus(TicketStatus status, Pageable pageable);
}
