package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IRestAddressController {

    ResponseEntity<AddressResponse> saveAddress(AddressRequest request);

    ResponseEntity<List<AddressResponse>> findAllAddress();

    ResponseEntity<AddressResponse> updateAddress(String id, AddressRequest request);

    ResponseEntity<String> deleteAddress(String id);

    ResponseEntity<AddressResponse> getAddressById(String id);
}
