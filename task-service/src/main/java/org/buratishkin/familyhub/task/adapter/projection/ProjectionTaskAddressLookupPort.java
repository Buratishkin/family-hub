package org.buratishkin.familyhub.task.adapter.projection;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.address.api.AddressView;
import org.buratishkin.familyhub.task.adapter.http.AddressServiceInternalClient;
import org.buratishkin.familyhub.task.port.TaskAddressLookupPort;
import org.buratishkin.familyhub.task.projection.TaskDependencyProjectionService;
import org.buratishkin.familyhub.task.projection.TaskAddressProjectionEntity;
import org.buratishkin.familyhub.task.projection.TaskAddressProjectionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectionTaskAddressLookupPort implements TaskAddressLookupPort {
    private final TaskAddressProjectionRepository addressRepository;
    private final TaskDependencyProjectionService projectionService;
    private final AddressServiceInternalClient addressServiceInternalClient;

    @Override
    public AddressView findAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .map(this::toView)
                .orElseGet(() -> fetchAndCacheAddress(addressId));
    }

    private AddressView toView(TaskAddressProjectionEntity address) {
        return new AddressView(
                address.getAddressId(),
                address.getFamilyId(),
                address.getCategoryId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private AddressView fetchAndCacheAddress(Long addressId) {
        try {
            AddressView address = addressServiceInternalClient.findAddressById(addressId);
            if (address != null) {
                projectionService.applyAddressChanged(address.id(), address.familyId(), address.categoryId());
            }
            return address;
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
