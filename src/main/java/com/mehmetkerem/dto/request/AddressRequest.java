package com.mehmetkerem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressRequest {

    @NotNull(message = "Adres başlığı boş olamaz.")
    private String title;

    @NotNull(message = "Ülke bilgisi boş olamaz.")
    private String country;

    @NotNull(message = "Şehir bilgisi boş olamaz.")
    private String city;

    @NotNull(message = "İlçe bilgisi boş olamaz.")
    private String district;

    @NotNull(message = "Posta kodu boş olamaz.")
    private String postalCode;

    @NotNull(message = "Adres satırı boş olamaz.")
    private String addressLine;
}
