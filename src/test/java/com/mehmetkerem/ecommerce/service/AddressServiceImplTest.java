package com.mehmetkerem.ecommerce.service;

import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.AddressMapper;
import com.mehmetkerem.model.Address;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.AddressRepository;
import com.mehmetkerem.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAddressById_success() {
        Address address = new Address();
        address.setId("1");
        when(addressRepository.findById("1")).thenReturn(Optional.of(address));

        Address result = addressService.getAddressById("1");
        assertEquals("1", result.getId());
    }

    @Test
    void testGetAddressById_notFound() {
        when(addressRepository.findById("2")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> addressService.getAddressById("2"));
    }

    @Test
    void testGetAddressResponseById_success() {
        Address address = new Address();
        address.setId("1");
        AddressResponse response = new AddressResponse();
        when(addressRepository.findById("1")).thenReturn(Optional.of(address));
        when(addressMapper.toResponse(address)).thenReturn(response);

        AddressResponse result = addressService.getAddressResponseById("1");
        assertNotNull(result);
    }

    @Test
    void testSaveAddress_success() {
        AddressRequest request = new AddressRequest();
        Address address = new Address();
        AddressResponse response = new AddressResponse();

        when(addressMapper.toEntity(request)).thenReturn(address);
        when(addressRepository.save(address)).thenReturn(address);
        when(addressMapper.toResponse(address)).thenReturn(response);

        AddressResponse result = addressService.saveAddress(request);
        assertNotNull(result);
    }

    @Test
    void testFindAllAddress_success() {
        Address address1 = new Address();
        Address address2 = new Address();
        List<Address> addresses = List.of(address1, address2);
        AddressResponse response1 = new AddressResponse();
        AddressResponse response2 = new AddressResponse();

        when(addressRepository.findAll()).thenReturn(addresses);
        when(addressMapper.toResponse(address1)).thenReturn(response1);
        when(addressMapper.toResponse(address2)).thenReturn(response2);

        List<AddressResponse> results = addressService.findAllAddress();
        assertEquals(2, results.size());
    }

    @Test
    void testUpdateAddress_success() {
        AddressRequest request = new AddressRequest();
        Address address = new Address();
        AddressResponse response = new AddressResponse();

        when(addressRepository.findById("1")).thenReturn(Optional.of(address));
        doNothing().when(addressMapper).update(address, request);
        when(addressRepository.save(address)).thenReturn(address);
        when(addressMapper.toResponse(address)).thenReturn(response);

        AddressResponse result = addressService.updateAddress("1", request);
        assertNotNull(result);
    }

    @Test
    void testUpdateAddress_notFound() {
        AddressRequest request = new AddressRequest();
        when(addressRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> addressService.updateAddress("2", request));
    }

    @Test
    void testDeleteAddress_success() {
        Address address = new Address();
        when(addressRepository.findById("1")).thenReturn(Optional.of(address));
        doNothing().when(addressRepository).delete(address);

        String result = addressService.deleteAddress("1");
        assertTrue(result.contains("1"));
    }

    @Test
    void testDeleteAddress_notFound() {
        when(addressRepository.findById("2")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> addressService.deleteAddress("2"));
    }

    @Test
    void testGetAddressesByUser_success() {
        Address address1 = new Address();
        Address address2 = new Address();
        AddressResponse response1 = new AddressResponse();
        AddressResponse response2 = new AddressResponse();

        User user = new User();
        user.setAddressIds(List.of("1", "2"));

        when(addressRepository.findById("1")).thenReturn(Optional.of(address1));
        when(addressRepository.findById("2")).thenReturn(Optional.of(address2));
        when(addressMapper.toResponse(address1)).thenReturn(response1);
        when(addressMapper.toResponse(address2)).thenReturn(response2);

        List<AddressResponse> results = addressService.getAddressesByUser(user);
        assertEquals(2, results.size());
    }

    @Test
    void testGetAddressesByUser_notFound() {
        User user = new User();
        user.setAddressIds(List.of("1"));
        when(addressRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> addressService.getAddressesByUser(user));
    }
}
