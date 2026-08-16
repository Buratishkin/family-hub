package org.buratishkin.familyhub.address.api;

import java.util.List;

public interface AddressLookupApi {
    AddressView findAddressById(Long id);

    List<AddressView> findAddressesByFamilyId(Long familyId);
}
