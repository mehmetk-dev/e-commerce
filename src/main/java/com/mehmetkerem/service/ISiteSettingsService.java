package com.mehmetkerem.service;

import com.mehmetkerem.model.SiteSettings;

public interface ISiteSettingsService {

    SiteSettings get();

    SiteSettings update(SiteSettings incoming);
}
