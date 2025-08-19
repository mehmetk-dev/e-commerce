package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.model.Address;

import java.util.List;

public interface IAddressService {

    AddressResponse saveAddress(AddressRequest request);
    List<AddressResponse> findAllAddress();
    AddressResponse updateAddress(String id, AddressRequest request);
    String deleteAddress(String id);
    Address getAddressById(String id);
    AddressResponse getAddressResponseById(String id);

}
