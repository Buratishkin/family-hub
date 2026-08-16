package org.buratishkin.familyhub.address.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.buratishkin.familyhub.shared.response.UpdateResp;
import org.buratishkin.familyhub.address.dto.*;
import org.buratishkin.familyhub.address.service.AddressManagerService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/address")
public class AddressController {
    private final AddressManagerService addressManagerService;

    @PostMapping("/")
    public CreateResp<AddressCreateResp> create(@Valid @RequestBody AddressCreateReq createReq, Authentication authentication){
        return addressManagerService.create(createReq, authentication);
    }

    @PutMapping("/{addressId}")
    public UpdateResp update(@Valid @RequestBody AddressUpdateReq updateReq, @PathVariable Long addressId, Authentication authentication){
        return addressManagerService.update(updateReq, addressId, authentication);
    }

    @DeleteMapping("/{addressId}")
    public DeleteResp delete(@Valid @RequestBody(required = false) AddressDeleteReq deleteReq,
                             @PathVariable Long addressId,
                             Authentication authentication){
        return addressManagerService.delete(deleteReq, addressId, authentication);
    }
}
