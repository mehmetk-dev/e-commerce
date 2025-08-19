package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestAddressController;
import com.mehmetkerem.dto.request.AddressRequest;
import com.mehmetkerem.dto.response.AddressResponse;
import com.mehmetkerem.service.IAddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/address")
public class RestAddressControllerImpl implements IRestAddressController {

    private final IAddressService addressService;

    public RestAddressControllerImpl(IAddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/save")
    @Override
    public ResponseEntity<AddressResponse> saveAddress(@Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.saveAddress(request));
    }

    @GetMapping("/find-all")
    @Override
    public ResponseEntity<List<AddressResponse>> findAllAddress() {
        return ResponseEntity.status(HttpStatus.OK).body(addressService.findAllAddress());
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable("id") String id, @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(addressService.updateAddress(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<String> deleteAddress(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.OK).body(addressService.deleteAddress(id));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<AddressResponse> getAddressById(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.OK).body(addressService.getAddressResponseById(id));
    }
}
