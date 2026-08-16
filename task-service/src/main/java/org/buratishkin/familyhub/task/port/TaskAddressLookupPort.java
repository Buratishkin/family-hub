package org.buratishkin.familyhub.task.port;

import org.buratishkin.familyhub.address.api.AddressView;

public interface TaskAddressLookupPort {
    AddressView findAddressById(Long addressId);
}
