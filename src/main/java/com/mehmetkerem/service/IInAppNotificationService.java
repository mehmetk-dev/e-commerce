package com.mehmetkerem.service;

import com.mehmetkerem.model.Notification;

import java.util.List;

public interface IInAppNotificationService {

    Notification create(Long userId, String title, String message, String type, Long referenceId);

    List<Notification> getByUser(Long userId);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);
}
