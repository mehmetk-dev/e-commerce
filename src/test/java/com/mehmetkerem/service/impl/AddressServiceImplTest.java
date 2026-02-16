package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.AddressMapper;
import com.mehmetkerem.model.Address;
import com.mehmetkerem.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AddressServiceImplTest {

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private static final Long USER_ID = 1L;
    private Address address;
    private AddressRequest addressRequest;
    private AddressResponse addressResponse;

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .id(1L)
                .userId(USER_ID)
                .title("Ev")
                .country("Turkey")
                .city("Istanbul")
                .district("Kadikoy")
                .postalCode("34710")
                .addressLine("Test sokak 5")
                .build();
        addressRequest = new AddressRequest();
        addressRequest.setTitle("Ev");
        addressRequest.setCountry("Turkey");
        addressRequest.setCity("Istanbul");
        addressRequest.setDistrict("Kadikoy");
        addressRequest.setPostalCode("34710");
        addressRequest.setAddressLine("Test sokak 5");
        addressResponse = new AddressResponse();
        addressResponse.setId(1L);
        addressResponse.setTitle("Ev");
        addressResponse.setCity("Istanbul");
    }

    @Test
    @DisplayName("getAddressById - adres bulunamazsa NotFoundException")
    void getAddressById_WhenNotExists_ShouldThrowNotFoundException() {
        when(addressRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> addressService.getAddressById(999L));
    }

    @Test
    @DisplayName("getAddressById - mevcut adres döner")
    void getAddressById_WhenExists_ShouldReturnAddress() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        Address result = addressService.getAddressById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Istanbul", result.getCity());
    }

    @Test
    @DisplayName("getAddressResponseById - response döner")
    void getAddressResponseById_ShouldReturnResponse() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(addressMapper.toResponse(address)).thenReturn(addressResponse);

        AddressResponse result = addressService.getAddressResponseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ev", result.getTitle());
    }

    @Test
    @DisplayName("getAddressByIdAndUserId - aynı kullanıcı adresi döner")
    void getAddressByIdAndUserId_WhenOwner_ShouldReturnAddress() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        Address result = addressService.getAddressByIdAndUserId(1L, USER_ID);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(USER_ID, result.getUserId());
    }

    @Test
    @DisplayName("getAddressByIdAndUserId - başka kullanıcının adresi BadRequestException")
    void getAddressByIdAndUserId_WhenNotOwner_ShouldThrow() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        assertThrows(BadRequestException.class, () -> addressService.getAddressByIdAndUserId(1L, 999L));
    }

    @Test
    @DisplayName("getAddressResponseByIdAndUserId - response döner")
    void getAddressResponseByIdAndUserId_ShouldReturnResponse() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(addressMapper.toResponse(address)).thenReturn(addressResponse);

        AddressResponse result = addressService.getAddressResponseByIdAndUserId(1L, USER_ID);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("updateAddressForUser - sahip günceller")
    void updateAddressForUser_ShouldUpdateAndReturn() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.toResponse(address)).thenReturn(addressResponse);

        AddressResponse result = addressService.updateAddressForUser(1L, USER_ID, addressRequest);

        assertNotNull(result);
        verify(addressMapper).update(eq(address), eq(addressRequest));
        verify(addressRepository).save(address);
    }

    @Test
    @DisplayName("deleteAddressForUser - sahip siler")
    void deleteAddressForUser_ShouldDeleteAndReturnMessage() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        doNothing().when(addressRepository).delete(address);

        String result = addressService.deleteAddressForUser(1L, USER_ID);

        assertTrue(result.contains("1"));
        assertTrue(result.contains("adres"));
        verify(addressRepository).delete(address);
    }

    @Test
    @DisplayName("saveAddress - adres kaydedilir ve userId set edilir")
    void saveAddress_ShouldSaveWithUserId() {
        when(addressMapper.toEntity(addressRequest)).thenReturn(address);
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> {
            Address a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(addressMapper.toResponse(any(Address.class))).thenReturn(addressResponse);

        AddressResponse result = addressService.saveAddress(USER_ID, addressRequest);

        assertNotNull(result);
        verify(addressRepository).save(argThat(a -> USER_ID.equals(a.getUserId())));
    }

    @Test
    @DisplayName("getAddressesByUserId - kullanıcının adresleri listelenir")
    void getAddressesByUserId_ShouldReturnUserAddresses() {
        when(addressRepository.findByUserId(USER_ID)).thenReturn(List.of(address));
        when(addressMapper.toResponse(address)).thenReturn(addressResponse);

        List<AddressResponse> result = addressService.getAddressesByUserId(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Ev", result.get(0).getTitle());
    }

    @Test
    @DisplayName("updateAddress - adres güncellenir")
    void updateAddress_WhenExists_ShouldUpdateAndReturn() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.toResponse(address)).thenReturn(addressResponse);

        AddressResponse result = addressService.updateAddress(1L, addressRequest);

        assertNotNull(result);
        verify(addressMapper).update(eq(address), eq(addressRequest));
        verify(addressRepository).save(address);
    }

    @Test
    @DisplayName("deleteAddress - adres silinir")
    void deleteAddress_WhenExists_ShouldDeleteAndReturnMessage() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        doNothing().when(addressRepository).delete(address);

        String result = addressService.deleteAddress(1L);

        assertTrue(result.contains("1"));
        assertTrue(result.contains("adres"));
        verify(addressRepository).delete(address);
    }
}
