package com.mehmetkerem.controller;

import com.mehmetkerem.controller.impl.RestAddressControllerImpl;
import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.service.IAddressService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestAddressControllerTest {

    @Mock
    private IAddressService addressService;

    @InjectMocks
    private RestAddressControllerImpl controller;

    private static final Long USER_ID = SecurityTestUtils.DEFAULT_USER_ID;
    private AddressRequest addressRequest;
    private AddressResponse addressResponse;

    @BeforeEach
    void setUp() {
        SecurityTestUtils.setCurrentUser();
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
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clearContext();
    }

    @Test
    @DisplayName("findAllAddress - admin ile liste döner")
    void findAllAddress_ShouldReturnList() {
        SecurityTestUtils.setCurrentUser(USER_ID, Role.ADMIN);
        when(addressService.findAllAddress()).thenReturn(List.of(addressResponse));

        ResultData<List<AddressResponse>> result = controller.findAllAddress();

        assertTrue(result.isStatus());
        assertEquals(1, result.getData().size());
        verify(addressService).findAllAddress();
    }

    @Test
    @DisplayName("getAddressById - kendi adresi döner")
    void getAddressById_ShouldReturnAddress() {
        when(addressService.getAddressResponseByIdAndUserId(1L, USER_ID)).thenReturn(addressResponse);

        ResultData<AddressResponse> result = controller.getAddressById(1L);

        assertTrue(result.isStatus());
        assertEquals(1L, result.getData().getId());
        verify(addressService).getAddressResponseByIdAndUserId(1L, USER_ID);
    }

    @Test
    @DisplayName("updateAddress - güncelleme başarılı")
    void updateAddress_ShouldReturnUpdated() {
        when(addressService.updateAddressForUser(1L, USER_ID, addressRequest)).thenReturn(addressResponse);

        ResultData<AddressResponse> result = controller.updateAddress(1L, addressRequest);

        assertTrue(result.isStatus());
        verify(addressService).updateAddressForUser(1L, USER_ID, addressRequest);
    }

    @Test
    @DisplayName("deleteAddress - silme mesajı döner")
    void deleteAddress_ShouldReturnMessage() {
        when(addressService.deleteAddressForUser(1L, USER_ID)).thenReturn("1 ID'li adres silinmiştir!");

        ResultData<String> result = controller.deleteAddress(1L);

        assertTrue(result.isStatus());
        assertTrue(result.getData().contains("1"));
        verify(addressService).deleteAddressForUser(1L, USER_ID);
    }

    @Test
    @DisplayName("saveAddress - kullanıcı adresi kaydedilir")
    void saveAddress_ShouldSaveForUser() {
        when(addressService.saveAddress(USER_ID, addressRequest)).thenReturn(addressResponse);

        ResultData<AddressResponse> result = controller.saveAddress(addressRequest);

        assertTrue(result.isStatus());
        assertEquals(1L, result.getData().getId());
        verify(addressService).saveAddress(USER_ID, addressRequest);
    }

    @Test
    @DisplayName("getMyAddresses - kullanıcının adresleri döner")
    void getMyAddresses_ShouldReturnUserAddresses() {
        when(addressService.getAddressesByUserId(USER_ID)).thenReturn(List.of(addressResponse));

        ResultData<List<AddressResponse>> result = controller.getMyAddresses();

        assertTrue(result.isStatus());
        assertEquals(1, result.getData().size());
        verify(addressService).getAddressesByUserId(USER_ID);
    }
}
