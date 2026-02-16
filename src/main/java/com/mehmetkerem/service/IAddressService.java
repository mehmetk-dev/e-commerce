package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.model.Address;

import java.util.List;

public interface IAddressService {

    AddressResponse saveAddress(Long userId, AddressRequest request);

    List<AddressResponse> getAddressesByUserId(Long userId);

    List<AddressResponse> findAllAddress();

    AddressResponse updateAddress(Long id, AddressRequest request);

    String deleteAddress(Long id);

    Address getAddressById(Long id);

    /**
     * Adresi getirir; sadece ilgili kullanıcıya aitse döner.
     * Başka kullanıcının adresi ise exception fırlatır.
     */
    Address getAddressByIdAndUserId(Long id, Long userId);

    AddressResponse getAddressResponseById(Long id);

    /** Kullanıcı sadece kendi adresini görebilir. */
    AddressResponse getAddressResponseByIdAndUserId(Long id, Long userId);

    /** Sadece adres sahibi güncelleyebilir. */
    AddressResponse updateAddressForUser(Long id, Long userId, AddressRequest request);

    /** Sadece adres sahibi silebilir. */
    String deleteAddressForUser(Long id, Long userId);
}
