package com.mehmetkerem.controller.impl;

import com.mehmetkerem.model.Notification;
import com.mehmetkerem.service.impl.InAppNotificationService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import com.mehmetkerem.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class RestNotificationController {

    private final InAppNotificationService notificationService;

    private static long requireCurrentUserId() {
        Long id = SecurityUtils.getCurrentUserId();
        if (id == null)
            throw new InsufficientAuthenticationException("Oturum gerekli");
        return id;
    }

    @GetMapping
    public ResultData<List<Notification>> getMyNotifications() {
        return ResultHelper.success(notificationService.getByUser(requireCurrentUserId()));
    }

    @GetMapping("/unread-count")
    public ResultData<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount(requireCurrentUserId());
        return ResultHelper.success(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResultData<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id, requireCurrentUserId());
        return ResultHelper.success("OK");
    }

    @PutMapping("/read-all")
    public ResultData<String> markAllAsRead() {
        notificationService.markAllAsRead(requireCurrentUserId());
        return ResultHelper.success("OK");
    }
}
