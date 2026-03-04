package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestActivityLogController;
import com.mehmetkerem.dto.response.ActivityLogResponse;
import com.mehmetkerem.dto.response.CursorResponse;
import com.mehmetkerem.enums.ActivityType;
import com.mehmetkerem.service.IActivityLogService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/activity-logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class RestActivityLogController implements IRestActivityLogController {

    private final IActivityLogService activityLogService;

    private static final int MAX_PAGE_SIZE = 100;

    @Override
    @GetMapping
    public ResultData<CursorResponse<ActivityLogResponse>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        int cappedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, cappedSize, sort);

        return ResultHelper.cursor(activityLogService.getAllLogs(pageable));
    }

    @Override
    @GetMapping("/user/{userId}")
    public ResultData<CursorResponse<ActivityLogResponse>> getLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        return ResultHelper.cursor(activityLogService.getLogsByUserId(userId, pageable));
    }

    @Override
    @GetMapping("/type/{type}")
    public ResultData<CursorResponse<ActivityLogResponse>> getLogsByType(
            @PathVariable ActivityType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        return ResultHelper.cursor(activityLogService.getLogsByType(type, pageable));
    }

    @Override
    @GetMapping("/search")
    public ResultData<CursorResponse<ActivityLogResponse>> searchLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) ActivityType type,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LocalDateTime fromDate = from != null ? LocalDateTime.parse(from) : null;
        LocalDateTime toDate = to != null ? LocalDateTime.parse(to) : null;
        Pageable pageable = PageRequest.of(page, Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by("createdAt").descending());

        return ResultHelper.cursor(activityLogService.searchLogs(userId, type, fromDate, toDate, pageable));
    }

    @Override
    @GetMapping("/user/{userId}/recent")
    public ResultData<List<ActivityLogResponse>> getRecentUserActivity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "7") int days) {

        return ResultHelper.success(activityLogService.getRecentActivityByUser(userId, days));
    }
}
