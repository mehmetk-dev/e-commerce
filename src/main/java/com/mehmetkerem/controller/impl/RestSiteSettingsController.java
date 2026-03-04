package com.mehmetkerem.controller.impl;

import com.mehmetkerem.model.SiteSettings;
import com.mehmetkerem.service.impl.SiteSettingsService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/site-settings")
@RequiredArgsConstructor
public class RestSiteSettingsController {

    private final SiteSettingsService service;

    @GetMapping
    public ResultData<SiteSettings> get() {
        return ResultHelper.success(service.get());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<SiteSettings> update(@RequestBody SiteSettings settings) {
        return ResultHelper.success(service.update(settings));
    }
}
