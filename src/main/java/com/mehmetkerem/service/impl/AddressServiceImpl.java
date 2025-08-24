package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.AddressMapper;
import com.mehmetkerem.model.Address;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.AddressRepository;
import com.mehmetkerem.service.IAddressService;
import com.mehmetkerem.util.Messages;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements IAddressService {

    private final AddressMapper addressMapper;
    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressMapper addressMapper, AddressRepository addressRepository) {
        this.addressMapper = addressMapper;
        this.addressRepository = addressRepository;
    }


    public Address getAddressById(String id) {
        return addressRepository.findById(id).orElseThrow(()
                -> new NotFoundException(String.format(ExceptionMessages.ADDRESS_NOT_FOUND, id)));
    }

    @Override
    public AddressResponse getAddressResponseById(String id) {
        return addressMapper.toResponse(getAddressById(id));
    }

    @Override
    public AddressResponse saveAddress(AddressRequest request) {
        return addressMapper.toResponse(addressRepository.save(addressMapper.toEntity(request)));
    }

    @Override
    public List<AddressResponse> findAllAddress() {
        List<Address> addressList = addressRepository.findAll();
        return addressList.stream().
                map(addressMapper::toResponse).
                toList();
    }

    @Override
    public AddressResponse updateAddress(String id, AddressRequest request) {
        Address currentAddress = getAddressById(id);
        addressMapper.update(currentAddress, request);
        return addressMapper.toResponse(addressRepository.save(currentAddress));
    }

    @Override
    public String deleteAddress(String id) {
        addressRepository.delete(getAddressById(id));
        return String.format(Messages.DELETE_VALUE, id, "adres");
    }

    public List<AddressResponse> getAddressesByUser(User user) {
        return user.getAddressIds().stream()
                .map(this::getAddressById)
                .map(addressMapper::toResponse)
                .toList();

    }

}
