package org.buratishkin.familyhub.address.service;

import org.buratishkin.familyhub.address.AddressEntity;
import org.buratishkin.familyhub.address.category.api.CategoryView;
import org.buratishkin.familyhub.address.category.service.CategoryCrudService;
import org.buratishkin.familyhub.address.dto.AddressCreateReq;
import org.buratishkin.familyhub.address.dto.AddressCreateResp;
import org.buratishkin.familyhub.address.dto.AddressDeleteReq;
import org.buratishkin.familyhub.address.enums.StreetType;
import org.buratishkin.familyhub.address.map.MapUrlBuilder;
import org.buratishkin.familyhub.address.mapper.AddressMapper;
import org.buratishkin.familyhub.address.port.AddressFamilyAccessPort;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressManagerServiceTest {
    @Mock
    private AddressCrudService addressCrudService;

    @Mock
    private CategoryCrudService categoryCrudService;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private MapUrlBuilder urlBuilder;

    @Mock
    private AddressFamilyAccessPort familyAccessPort;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AddressManagerService addressManagerService;

    @Test
    void createAllowsLegacySystemCategoryWithoutType() {
        AddressCreateReq request = createRequest("Москва", 300L);
        AddressEntity address = address(null, 10L);
        CategoryView category = new CategoryView(300L, null, "дом", null, false, null);
        when(authentication.getName()).thenReturn("user");
        when(familyAccessPort.currentUser("user")).thenReturn(new UserView(1L, "user", "user@example.com"));
        when(familyAccessPort.hasFamilyAccess(1L, 10L)).thenReturn(true);
        when(categoryCrudService.existsById(300L)).thenReturn(true);
        when(categoryCrudService.findCategoryById(300L)).thenReturn(category);
        when(addressMapper.parseStreetType("street")).thenReturn(StreetType.STREET);
        when(familyAccessPort.familyExists(10L)).thenReturn(true);
        when(addressMapper.toEntity(request, 10L, 300L)).thenReturn(address);
        when(addressCrudService.save(address)).thenAnswer(invocation -> {
            address.setId(200L);
            return address;
        });

        CreateResp<AddressCreateResp> response = addressManagerService.create(request, authentication);

        assertThat(response.isResult()).isTrue();
        assertThat(response.getCreateResp().addressId()).isEqualTo(200L);
        verify(addressCrudService).save(address);
        verify(domainEventPublisher).publish(any());
    }

    @Test
    void createReturnsInvalidCityBeforeSave() {
        AddressCreateReq request = createRequest(" ", 300L);
        CategoryView category = new CategoryView(300L, 10L, "дом", "CUSTOM", false, null);
        when(authentication.getName()).thenReturn("user");
        when(familyAccessPort.currentUser("user")).thenReturn(new UserView(1L, "user", "user@example.com"));
        when(familyAccessPort.hasFamilyAccess(1L, 10L)).thenReturn(true);
        when(categoryCrudService.existsById(300L)).thenReturn(true);
        when(categoryCrudService.findCategoryById(300L)).thenReturn(category);
        when(addressMapper.parseStreetType("street")).thenReturn(StreetType.STREET);

        CreateResp<AddressCreateResp> response = addressManagerService.create(request, authentication);

        assertThat(response.isResult()).isFalse();
        assertThat(response.getReason()).isEqualTo("invalid city");
        verify(addressCrudService, never()).save(any());
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void deleteUsesAddressFamilyWhenBodyIsAbsent() {
        AddressEntity address = address(200L, 10L);
        when(authentication.getName()).thenReturn("user");
        when(familyAccessPort.currentUser("user")).thenReturn(new UserView(1L, "user", "user@example.com"));
        when(addressCrudService.existsById(200L)).thenReturn(true);
        when(addressCrudService.findById(200L)).thenReturn(address);
        when(familyAccessPort.hasFamilyAccess(1L, 10L)).thenReturn(true);

        DeleteResp response = addressManagerService.delete(null, 200L, authentication);

        assertThat(response.result()).isTrue();
        assertThat(response.reason()).isEqualTo("good data");
        verify(addressCrudService).delete(address);
        verify(domainEventPublisher).publish(any());
    }

    @Test
    void deleteRejectsMismatchedBodyFamilyId() {
        AddressEntity address = address(200L, 10L);
        when(authentication.getName()).thenReturn("user");
        when(familyAccessPort.currentUser("user")).thenReturn(new UserView(1L, "user", "user@example.com"));
        when(addressCrudService.existsById(200L)).thenReturn(true);
        when(addressCrudService.findById(200L)).thenReturn(address);

        DeleteResp response = addressManagerService.delete(new AddressDeleteReq(99L), 200L, authentication);

        assertThat(response.result()).isFalse();
        assertThat(response.reason()).isEqualTo("invalid familyId");
        verify(addressCrudService, never()).delete(any());
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void deleteReturnsInvalidAddressIdWhenAddressDoesNotExist() {
        when(authentication.getName()).thenReturn("user");
        when(familyAccessPort.currentUser("user")).thenReturn(new UserView(1L, "user", "user@example.com"));
        when(addressCrudService.existsById(404L)).thenReturn(false);

        DeleteResp response = addressManagerService.delete(null, 404L, authentication);

        assertThat(response.result()).isFalse();
        assertThat(response.reason()).isEqualTo("invalid addressId");
        verify(addressCrudService, never()).delete(any());
        verify(domainEventPublisher, never()).publish(any());
    }

    private static AddressEntity address(Long id, Long familyId) {
        AddressEntity address = new AddressEntity();
        address.setId(id);
        address.setFamilyId(familyId);
        address.setCategoryId(300L);
        address.setName("Home");
        return address;
    }

    private static AddressCreateReq createRequest(String city, Long categoryId) {
        return new AddressCreateReq(
                10L,
                categoryId,
                "Home",
                "Россия",
                city,
                "street",
                "Ленина",
                "1",
                null,
                null
        );
    }
}
