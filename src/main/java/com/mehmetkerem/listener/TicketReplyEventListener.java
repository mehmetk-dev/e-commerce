package com.mehmetkerem.listener;

import com.mehmetkerem.service.impl.InAppNotificationService;
import com.mehmetkerem.service.impl.SupportTicketServiceImpl.TicketReplyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketReplyEventListener {

    private final InAppNotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTicketReply(TicketReplyEvent event) {
        try {
            notificationService.create(
                    event.userId(),
                    "Destek talebiniz yanıtlandı",
                    "\"" + event.subject() + "\" konulu talebinize yanıt verildi.",
                    "TICKET_REPLY",
                    event.ticketId());
        } catch (Exception e) {
            log.error("Destek talebi bildirimi gönderilemedi. Ticket ID: {}", event.ticketId(), e);
        }
    }
}
