package com.mehmetkerem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "address")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {

    @Id
    private String id;

    private String title;

    private String country;

    private String city;

    private String district;

    @Field("address_line")
    private String addressLine;

    @Field("postal_code")
    private String postalCode;
}
