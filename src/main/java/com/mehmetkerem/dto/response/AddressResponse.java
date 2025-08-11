package com.mehmetkerem.dto.response;

import lombok.Data;

@Data
public class AddressResponse {

    private String id;

    private String title;

    private String country;

    private String city;

    private String district;

    private String postalCode;

    private String addressLine;
}
