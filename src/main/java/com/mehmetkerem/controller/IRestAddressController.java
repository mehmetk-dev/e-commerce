package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.util.ResultData;
import java.util.List;

public interface IRestAddressController {
    ResultData<AddressResponse> saveAddress(AddressRequest request);

    ResultData<List<AddressResponse>> findAllAddress();

    ResultData<AddressResponse> updateAddress(Long id, AddressRequest request);

    ResultData<String> deleteAddress(Long id);

    ResultData<AddressResponse> getAddressById(Long id);

    ResultData<List<AddressResponse>> getMyAddresses();
}
