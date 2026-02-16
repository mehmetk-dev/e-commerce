package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestStatsController;
import com.mehmetkerem.dto.response.StatsResponse;
import com.mehmetkerem.service.IStatsService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/stats")
@RequiredArgsConstructor
public class RestStatsControllerImpl implements IRestStatsController {

    private final IStatsService statsService;

    @Override
    @Secured("ROLE_ADMIN")
    @GetMapping
    public ResultData<StatsResponse> getStats() {
        return ResultHelper.success(statsService.getAdminStats());
    }
}
