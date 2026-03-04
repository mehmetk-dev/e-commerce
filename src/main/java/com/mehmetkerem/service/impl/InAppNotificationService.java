package com.mehmetkerem.service.impl;

import com.mehmetkerem.model.Notification;
import com.mehmetkerem.repository.NotificationRepository;
import com.mehmetkerem.service.IInAppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InAppNotificationService implements IInAppNotificationService {

    private final NotificationRepository repository;

    @Override
    @Transactional
    public Notification create(Long userId, String title, String message, String type, Long referenceId) {
        return repository.save(Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .build());
    }

    @Override
    public List<Notification> getByUser(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        repository.findById(notificationId).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setRead(true);
                repository.save(n);
            }
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        repository.markAllAsReadByUserId(userId);
    }
}
