package com.mehmetkerem.service.impl;

import com.mehmetkerem.model.SiteSettings;
import com.mehmetkerem.repository.SiteSettingsRepository;
import com.mehmetkerem.service.ISiteSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SiteSettingsService implements ISiteSettingsService {

    private final SiteSettingsRepository repository;

    /**
     * Tek satır döndürür; yoksa default değerlerle oluşturur.
     */
    @Override
    public SiteSettings get() {
        return repository.findAll().stream().findFirst()
                .orElseGet(() -> repository.save(SiteSettings.builder().build()));
    }

    @Override
    @Transactional
    public SiteSettings update(SiteSettings incoming) {
        SiteSettings existing = get();
        existing.setStoreName(incoming.getStoreName());
        existing.setBusinessType(incoming.getBusinessType());
        existing.setStoreDescription(incoming.getStoreDescription());
        existing.setPhone(incoming.getPhone());
        existing.setEmail(incoming.getEmail());
        existing.setWebsite(incoming.getWebsite());
        existing.setAddress(incoming.getAddress());
        existing.setWhatsapp(incoming.getWhatsapp());
        existing.setWeekdayHours(incoming.getWeekdayHours());
        existing.setSaturdayHours(incoming.getSaturdayHours());
        existing.setStandardDelivery(incoming.getStandardDelivery());
        existing.setExpressDelivery(incoming.getExpressDelivery());
        existing.setFreeShippingMin(incoming.getFreeShippingMin());
        existing.setMetaTitle(incoming.getMetaTitle());
        existing.setMetaDescription(incoming.getMetaDescription());
        return repository.save(existing);
    }
}
