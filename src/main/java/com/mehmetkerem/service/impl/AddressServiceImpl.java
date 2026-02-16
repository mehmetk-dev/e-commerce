package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.AddressMapper;
import com.mehmetkerem.model.Address;
import com.mehmetkerem.repository.AddressRepository;
import com.mehmetkerem.service.IAddressService;
import com.mehmetkerem.util.Messages;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SuppressWarnings("null")
public class AddressServiceImpl implements IAddressService {

    private final AddressMapper addressMapper;
    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressMapper addressMapper, AddressRepository addressRepository) {
        this.addressMapper = addressMapper;
        this.addressRepository = addressRepository;
    }

    @Override
    public Address getAddressById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(ExceptionMessages.ADDRESS_NOT_FOUND, id)));
    }

    @Override
    public Address getAddressByIdAndUserId(Long id, Long userId) {
        Address address = getAddressById(id);
        if (!userId.equals(address.getUserId())) {
            throw new BadRequestException("Bu adres size ait değil.");
        }
        return address;
    }

    @Override
    public AddressResponse getAddressResponseById(Long id) {
        return addressMapper.toResponse(getAddressById(id));
    }

    @Override
    public AddressResponse getAddressResponseByIdAndUserId(Long id, Long userId) {
        return addressMapper.toResponse(getAddressByIdAndUserId(id, userId));
    }

    @Override
    public AddressResponse updateAddressForUser(Long id, Long userId, AddressRequest request) {
        Address currentAddress = getAddressByIdAndUserId(id, userId);
        addressMapper.update(currentAddress, request);
        return addressMapper.toResponse(addressRepository.save(currentAddress));
    }

    @Override
    public String deleteAddressForUser(Long id, Long userId) {
        Address address = getAddressByIdAndUserId(id, userId);
        addressRepository.delete(address);
        return String.format(Messages.DELETE_VALUE, id, "adres");
    }

    @Override
    public AddressResponse saveAddress(Long userId, AddressRequest request) {
        Address address = addressMapper.toEntity(request);
        address.setUserId(userId);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    public List<AddressResponse> findAllAddress() {
        List<Address> addressList = addressRepository.findAll();
        return addressList.stream().map(addressMapper::toResponse).toList();
    }

    @Override
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        Address currentAddress = getAddressById(id);
        addressMapper.update(currentAddress, request);
        return addressMapper.toResponse(addressRepository.save(currentAddress));
    }

    @Override
    public String deleteAddress(Long id) {
        addressRepository.delete(getAddressById(id));
        return String.format(Messages.DELETE_VALUE, id, "adres");
    }

    @Override
    public List<AddressResponse> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

}
