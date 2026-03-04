package com.mehmetkerem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "site_settings")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SiteSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mağaza
    @Builder.Default
    private String storeName = "Can Antika";
    @Builder.Default
    private String businessType = "Antika Eşya Satışı";
    @Builder.Default
    @Column(length = 500)
    private String storeDescription = "1989'den beri İstanbul'da en kaliteli antika eşyaları sunuyoruz.";

    // İletişim
    @Builder.Default
    private String phone = "+90 (212) 555-0123";
    @Builder.Default
    private String email = "info@canantika.com";
    @Builder.Default
    private String website = "www.canantika.com";
    @Builder.Default
    private String address = "Çukurcuma Caddesi No: 45, Beyoğlu, İstanbul";
    @Builder.Default
    private String whatsapp = "+90 (212) 555-0123";

    // Çalışma Saatleri
    @Builder.Default
    private String weekdayHours = "10:00 - 18:00";
    @Builder.Default
    private String saturdayHours = "11:00 - 17:00";

    // Teslimat
    @Builder.Default
    private String standardDelivery = "3-5 iş günü";
    @Builder.Default
    private String expressDelivery = "1-2 iş günü";
    @Builder.Default
    private Integer freeShippingMin = 500;

    // SEO
    @Builder.Default
    private String metaTitle = "Can Antika - Premium Antika Eşya Satışı İstanbul";
    @Builder.Default
    @Column(length = 500)
    private String metaDescription = "1989'den beri İstanbul'da en kaliteli antika eşyaları. Osmanlı, Viktoryen ve sanat eserleri.";
}
