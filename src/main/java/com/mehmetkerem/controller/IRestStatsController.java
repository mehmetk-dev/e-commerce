package com.mehmetkerem.controller;

import com.mehmetkerem.dto.response.StatsResponse;
import com.mehmetkerem.util.ResultData;

public interface IRestStatsController {
    ResultData<StatsResponse> getStats();
}
