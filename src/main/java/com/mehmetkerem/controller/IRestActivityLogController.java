package com.mehmetkerem.controller;

import com.mehmetkerem.dto.response.ActivityLogResponse;
import com.mehmetkerem.dto.response.CursorResponse;
import com.mehmetkerem.enums.ActivityType;
import com.mehmetkerem.util.ResultData;

import java.util.List;

public interface IRestActivityLogController {

    ResultData<CursorResponse<ActivityLogResponse>> getAllLogs(int page, int size, String sortBy, String direction);

    ResultData<CursorResponse<ActivityLogResponse>> getLogsByUser(Long userId, int page, int size);

    ResultData<CursorResponse<ActivityLogResponse>> getLogsByType(ActivityType type, int page, int size);

    ResultData<CursorResponse<ActivityLogResponse>> searchLogs(
            Long userId, ActivityType type, String from, String to, int page, int size);

    ResultData<List<ActivityLogResponse>> getRecentUserActivity(Long userId, int days);
}
