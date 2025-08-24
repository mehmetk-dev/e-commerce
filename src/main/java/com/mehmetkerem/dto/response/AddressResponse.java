package com.mehmetkerem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressResponse {

    private String id;

    private String title;

    private String country;

    private String city;

    private String district;

    private String postalCode;

    private String addressLine;
}
