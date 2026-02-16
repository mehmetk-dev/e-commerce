package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestAddressController;
import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.service.IAddressService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import com.mehmetkerem.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/address")
@RequiredArgsConstructor
public class RestAddressControllerImpl implements IRestAddressController {

    private final IAddressService addressService;

    private static long requireCurrentUserId() {
        Long id = SecurityUtils.getCurrentUserId();
        if (id == null) {
            throw new org.springframework.security.authentication.InsufficientAuthenticationException("Oturum gerekli");
        }
        return id;
    }

    @Override
    @PostMapping("/save")
    public ResultData<AddressResponse> saveAddress(@Valid @RequestBody AddressRequest request) {
        return ResultHelper.success(addressService.saveAddress(requireCurrentUserId(), request));
    }

    @Override
    @GetMapping("/find-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<List<AddressResponse>> findAllAddress() {
        return ResultHelper.success(addressService.findAllAddress());
    }

    @Override
    @PutMapping("/{id}")
    public ResultData<AddressResponse> updateAddress(@PathVariable("id") Long id,
            @RequestBody AddressRequest request) {
        return ResultHelper.success(addressService.updateAddressForUser(id, requireCurrentUserId(), request));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResultData<String> deleteAddress(@PathVariable("id") Long id) {
        return ResultHelper.success(addressService.deleteAddressForUser(id, requireCurrentUserId()));
    }

    @Override
    @GetMapping("/{id}")
    public ResultData<AddressResponse> getAddressById(@PathVariable("id") Long id) {
        return ResultHelper.success(addressService.getAddressResponseByIdAndUserId(id, requireCurrentUserId()));
    }

    @Override
    @GetMapping("/my-addresses")
    public ResultData<List<AddressResponse>> getMyAddresses() {
        return ResultHelper.success(addressService.getAddressesByUserId(requireCurrentUserId()));
    }
}
