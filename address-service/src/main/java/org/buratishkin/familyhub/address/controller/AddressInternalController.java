package org.buratishkin.familyhub.address.controller;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.address.api.AddressView;
import org.buratishkin.familyhub.address.service.AddressCrudService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/addresses")
public class AddressInternalController {
    private final AddressCrudService addressCrudService;

    @GetMapping("/{addressId}")
    public AddressView findAddressById(@PathVariable Long addressId) {
        return addressCrudService.findAddressById(addressId);
    }

    @GetMapping("/families/{familyId}")
    public List<AddressView> findAddressesByFamilyId(@PathVariable Long familyId) {
        return addressCrudService.findAddressesByFamilyId(familyId);
    }
}
