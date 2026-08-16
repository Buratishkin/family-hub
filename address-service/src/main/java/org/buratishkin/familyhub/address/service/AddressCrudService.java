package org.buratishkin.familyhub.address.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.address.AddressEntity;
import org.buratishkin.familyhub.address.AddressRepository;
import org.buratishkin.familyhub.address.api.AddressLookupApi;
import org.buratishkin.familyhub.address.api.AddressView;
import org.buratishkin.familyhub.address.exception.AddressNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressCrudService implements AddressLookupApi {
    private final AddressRepository addressRepository;

    public AddressEntity findById(Long id){
        return addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("Нет адреса с id: " + id));
    }

    public boolean existsById(Long id) {
        return addressRepository.existsById(id);
    }

    public AddressEntity save(AddressEntity address){
        return addressRepository.save(address);
    }

    public void delete(AddressEntity address) {
        addressRepository.delete(address);
    }

    public List<AddressEntity> findAllByFamilyId(Long familyId) {
        return addressRepository.findAllByFamilyId(familyId);
    }

    public AddressView findAddressById(Long id) {
        return toView(findById(id));
    }

    public List<AddressView> findAddressesByFamilyId(Long familyId) {
        return findAllByFamilyId(familyId).stream()
                .map(this::toView)
                .toList();
    }

    private AddressView toView(AddressEntity address) {
        if (address == null) {
            return null;
        }
        return new AddressView(
                address.getId(),
                address.getFamilyId(),
                address.getCategoryId(),
                address.getName(),
                address.getCountry(),
                address.getCity(),
                address.getStreetType() == null ? null : address.getStreetType().name(),
                address.getStreet(),
                address.getHouse(),
                address.getApartment(),
                address.getComment()
        );
    }

    //todo сделать замену всех данных адреса
}
