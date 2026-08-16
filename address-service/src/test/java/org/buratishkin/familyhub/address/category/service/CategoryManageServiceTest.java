package org.buratishkin.familyhub.address.category.service;

import org.buratishkin.familyhub.address.category.CategoryEntity;
import org.buratishkin.familyhub.address.category.CategoryType;
import org.buratishkin.familyhub.address.category.dto.CategoryDeleteReq;
import org.buratishkin.familyhub.address.port.AddressFamilyAccessPort;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
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
class CategoryManageServiceTest {
    @Mock
    private CategoryCrudService categoryCrudService;

    @Mock
    private AddressFamilyAccessPort familyAccessPort;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CategoryManageService categoryManageService;

    @Test
    void deleteUsesCategoryFamilyWhenBodyIsAbsent() {
        CategoryEntity category = category(300L, 10L, CategoryType.CUSTOM);
        when(authentication.getName()).thenReturn("user");
        when(familyAccessPort.currentUser("user")).thenReturn(new UserView(1L, "user", "user@example.com"));
        when(categoryCrudService.existsById(300L)).thenReturn(true);
        when(categoryCrudService.findById(300L)).thenReturn(category);
        when(familyAccessPort.hasFamilyAccess(1L, 10L)).thenReturn(true);

        DeleteResp response = categoryManageService.delete(null, 300L, authentication);

        assertThat(response.result()).isTrue();
        assertThat(response.reason()).isEqualTo("good data");
        assertThat(category.isArchived()).isTrue();
        verify(categoryCrudService).save(category);
        verify(domainEventPublisher).publish(any());
    }

    @Test
    void deleteRejectsMismatchedBodyFamilyId() {
        CategoryEntity category = category(300L, 10L, CategoryType.CUSTOM);
        when(authentication.getName()).thenReturn("user");
        when(familyAccessPort.currentUser("user")).thenReturn(new UserView(1L, "user", "user@example.com"));
        when(categoryCrudService.existsById(300L)).thenReturn(true);
        when(categoryCrudService.findById(300L)).thenReturn(category);

        DeleteResp response = categoryManageService.delete(new CategoryDeleteReq(99L), 300L, authentication);

        assertThat(response.result()).isFalse();
        assertThat(response.reason()).isEqualTo("invalid familyId");
        verify(categoryCrudService, never()).save(any());
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void deleteRejectsSystemCategoryWithClearReason() {
        CategoryEntity category = category(1L, null, CategoryType.SYSTEM);
        when(authentication.getName()).thenReturn("user");
        when(familyAccessPort.currentUser("user")).thenReturn(new UserView(1L, "user", "user@example.com"));
        when(categoryCrudService.existsById(1L)).thenReturn(true);
        when(categoryCrudService.findById(1L)).thenReturn(category);

        DeleteResp response = categoryManageService.delete(new CategoryDeleteReq(10L), 1L, authentication);

        assertThat(response.result()).isFalse();
        assertThat(response.reason()).isEqualTo("system category cannot be deleted");
        verify(categoryCrudService, never()).save(any());
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void deleteReturnsInvalidCategoryIdWhenCategoryDoesNotExist() {
        when(authentication.getName()).thenReturn("user");
        when(familyAccessPort.currentUser("user")).thenReturn(new UserView(1L, "user", "user@example.com"));
        when(categoryCrudService.existsById(404L)).thenReturn(false);

        DeleteResp response = categoryManageService.delete(null, 404L, authentication);

        assertThat(response.result()).isFalse();
        assertThat(response.reason()).isEqualTo("invalid categoryId");
        verify(categoryCrudService, never()).save(any());
        verify(domainEventPublisher, never()).publish(any());
    }

    private static CategoryEntity category(Long id, Long familyId, CategoryType type) {
        CategoryEntity category = new CategoryEntity();
        category.setId(id);
        category.setOwnerFamilyId(familyId);
        category.setType(type);
        category.setName("home");
        category.setArchived(false);
        return category;
    }
}
