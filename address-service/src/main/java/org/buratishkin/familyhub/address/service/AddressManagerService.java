package org.buratishkin.familyhub.address.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.buratishkin.familyhub.shared.response.UpdateResp;
import org.buratishkin.familyhub.address.AddressEntity;
import org.buratishkin.familyhub.address.api.event.AddressCreatedEvent;
import org.buratishkin.familyhub.address.api.event.AddressDeletedEvent;
import org.buratishkin.familyhub.address.api.event.AddressUpdatedEvent;
import org.buratishkin.familyhub.address.dto.AddressCreateReq;
import org.buratishkin.familyhub.address.dto.AddressCreateResp;
import org.buratishkin.familyhub.address.dto.AddressDeleteReq;
import org.buratishkin.familyhub.address.dto.AddressUpdateReq;
import org.buratishkin.familyhub.address.category.api.CategoryView;
import org.buratishkin.familyhub.address.category.service.CategoryCrudService;
import org.buratishkin.familyhub.address.enums.StreetType;
import org.buratishkin.familyhub.address.port.AddressFamilyAccessPort;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.address.map.MapUrlBuilder;
import org.buratishkin.familyhub.address.mapper.AddressMapper;
import org.buratishkin.familyhub.shared.event.DomainEvent;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressManagerService {
    private final AddressCrudService addressCrudService;
    private final CategoryCrudService categoryCrudService;
    private final AddressMapper addressMapper;
    private final MapUrlBuilder urlBuilder;
    private final AddressFamilyAccessPort familyAccessPort;
    private final DomainEventPublisher domainEventPublisher;

    public Map<String, String> buildAddressQuery(Long addressId){
        AddressEntity address = addressCrudService.findById(addressId);

        StringBuilder url = new StringBuilder();
        if (address.getCountry() != null)
            url.append(address.getCountry()).append(" ");

        url.append("г ").append(address.getCity()).append(" ");
        url.append((address.getStreetType().getRussianName())).append(" ");
        url.append(address.getStreet()).append(" ");

        if (address.getHouse() != null)
            url.append("д ").append(address.getHouse());

        String query = url.toString().trim();
        Map<String, String> links = new LinkedHashMap<>();
        links.put("Яндекс Карты", urlBuilder.yandexByAddress(query));
        links.put("Google Maps", urlBuilder.googleByAddress(query));
        links.put("2GIS", urlBuilder.twoGisByAddress(query));
        return links;
    }

    @Transactional
    public CreateResp<AddressCreateResp> create(AddressCreateReq createReq, Authentication authentication) {
        Long familyId = createReq.familyId();
        UserView user = familyAccessPort.currentUser(authentication.getName());

        CreateResp<AddressCreateResp> access = validateFamilyAccess(user.id(), familyId, this::getCreateAccessDeniedResult);
        if (access != null)
            return access;

        if (!isCategoryValidForFamily(createReq.categoryId(), familyId)){
            return getCreateInvalidCategoryResult();
        }

        StreetType streetType = addressMapper.parseStreetType(createReq.streetType());
        if (streetType == null) {
            return getCreateInvalidStreetTypeResult();
        }

        if (createReq.city() == null || createReq.city().isBlank()) {
            return getCreateInvalidCityResult();
        }

        if (!familyAccessPort.familyExists(createReq.familyId())) {
            return getCreateFamilyNotFoundResult();
        }
        CategoryView category = categoryCrudService.findCategoryById(createReq.categoryId());
        AddressEntity address = addressMapper.toEntity(createReq, createReq.familyId(), category.id());
        address.setStreetType(streetType);

        addressCrudService.save(address);
        safePublish(new AddressCreatedEvent(
                address.getId(),
                address.getFamilyId(),
                address.getCategoryId(),
                user.id(),
                address.getName(),
                LocalDateTime.now()
        ));
        return getCreateTrueResult(new AddressCreateResp(address.getId()));
    }

    @Transactional
    public UpdateResp update(AddressUpdateReq updateReq, Long addressId, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        Long familyId = updateReq.familyId();

        UpdateResp access = validateFamilyAccess(user.id(), familyId, this::getUpdateAccessDeniedResult);
        if (access != null) {
            return access;
        }

        if (!isCategoryValidForFamily(updateReq.categoryId(), familyId)) {
            return getUpdateInvalidCategoryResult();
        }

        AddressEntity address = addressCrudService.findById(addressId);
        if (!isAddressValidForFamily(address, familyId)) {
            return getUpdateInvalidAddressFamilyResult();
        }

        StreetType streetType = addressMapper.parseStreetType(updateReq.streetType());
        if (streetType == null) {
            return getUpdateInvalidStreetTypeResult();
        }

        if (updateReq.city() == null || updateReq.city().isBlank()) {
            return getUpdateInvalidCityResult();
        }

        CategoryView category = categoryCrudService.findCategoryById(updateReq.categoryId());
        addressMapper.applyUpdate(updateReq, address, category.id(), streetType);

        addressCrudService.save(address);
        safePublish(new AddressUpdatedEvent(
                address.getId(),
                address.getFamilyId(),
                address.getCategoryId(),
                user.id(),
                address.getName(),
                LocalDateTime.now()
        ));
        return getUpdateTrueResult();
    }

    @Transactional
    public DeleteResp delete(AddressDeleteReq deleteReq, Long addressId, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!addressCrudService.existsById(addressId)) {
            return getDeleteInvalidAddressResult();
        }

        AddressEntity address = addressCrudService.findById(addressId);
        Long familyId = address.getFamilyId();
        if (deleteReq != null && deleteReq.familyId() != null && !Objects.equals(deleteReq.familyId(), familyId)) {
            return getDeleteFalseResult();
        }

        DeleteResp access = validateFamilyAccess(user.id(), familyId, this::getDeleteFalseResult);
        if (access != null) {
            return access;
        }

        if (!isAddressValidForFamily(address, familyId)) {
            return getDeleteFalseResult();
        }

        String addressName = address.getName();
        addressCrudService.delete(address);
        safePublish(new AddressDeletedEvent(
                addressId,
                familyId,
                user.id(),
                addressName,
                LocalDateTime.now()
        ));
        return getDeleteTrueResult();
    }

    private void safePublish(DomainEvent event) {
        try {
            domainEventPublisher.publish(event);
        } catch (RuntimeException e) {
            log.warn("Failed to publish address domain event {}", event.getClass().getName(), e);
        }
    }

    private boolean isCategoryValidForFamily(Long categoryId, Long familyId) {
        if (categoryId == null || familyId == null) {
            return false;
        }
        if (!categoryCrudService.existsById(categoryId)) {
            return false;
        }
        CategoryView category = categoryCrudService.findCategoryById(categoryId);
        if (category.archived()) {
            return false;
        }
        return isSystemCategory(category) || Objects.equals(category.familyId(), familyId);
    }

    private boolean isSystemCategory(CategoryView category) {
        return category.familyId() == null;
    }

    private boolean isAddressValidForFamily(AddressEntity address, Long familyId) {
        return Objects.equals(address.getFamilyId(), familyId);
    }

    private <T> T validateFamilyAccess(
            Long userId,
            Long familyId,
            Supplier<T> falseResultSupplier
    ) {
        if (!familyAccessPort.hasFamilyAccess(userId, familyId)) {
            return falseResultSupplier.get();
        }

        return null;
    }

    private CreateResp<AddressCreateResp> getCreateAccessDeniedResult(){
        return new CreateResp<>(
                false,
                "access denied for familyId",
                null
        );
    }

    private CreateResp<AddressCreateResp> getCreateFamilyNotFoundResult(){
        return new CreateResp<>(
                false,
                "family not found",
                null
        );
    }

    private CreateResp<AddressCreateResp> getCreateInvalidCategoryResult(){
        return new CreateResp<>(
                false,
                "invalid categoryId",
                null
        );
    }

    private CreateResp<AddressCreateResp> getCreateInvalidStreetTypeResult(){
        return new CreateResp<>(
                false,
                "invalid streetType",
                null
        );
    }

    private CreateResp<AddressCreateResp> getCreateInvalidCityResult(){
        return new CreateResp<>(
                false,
                "invalid city",
                null
        );
    }

    private CreateResp<AddressCreateResp> getCreateTrueResult(AddressCreateResp createResp){
        return new CreateResp<>(
                true,
                "good data",
                createResp
        );
    }

    private UpdateResp getUpdateAccessDeniedResult(){
        return new UpdateResp(
                false,
                "access denied for familyId"
        );
    }

    private UpdateResp getUpdateInvalidCategoryResult(){
        return new UpdateResp(
                false,
                "invalid categoryId"
        );
    }

    private UpdateResp getUpdateInvalidStreetTypeResult(){
        return new UpdateResp(
                false,
                "invalid streetType"
        );
    }

    private UpdateResp getUpdateInvalidCityResult(){
        return new UpdateResp(
                false,
                "invalid city"
        );
    }

    private UpdateResp getUpdateInvalidAddressFamilyResult(){
        return new UpdateResp(
                false,
                "address does not belong to familyId"
        );
    }

    private UpdateResp getUpdateTrueResult(){
        return new UpdateResp(
                true,
                "good data"
        );
    }

    private DeleteResp getDeleteFalseResult(){
        return new DeleteResp(
                false,
                "invalid familyId"
        );
    }

    private DeleteResp getDeleteInvalidAddressResult(){
        return new DeleteResp(
                false,
                "invalid addressId"
        );
    }

    private DeleteResp getDeleteTrueResult(){
        return new DeleteResp(
                true,
                "good data"
        );
    }
}
