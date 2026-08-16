package org.buratishkin.familyhub.address.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.buratishkin.familyhub.shared.response.UpdateResp;
import org.buratishkin.familyhub.address.category.CategoryEntity;
import org.buratishkin.familyhub.address.category.api.event.CategoryArchivedEvent;
import org.buratishkin.familyhub.address.category.api.event.CategoryCreatedEvent;
import org.buratishkin.familyhub.address.category.api.event.CategoryUpdatedEvent;
import org.buratishkin.familyhub.address.category.dto.CategoryCreateReq;
import org.buratishkin.familyhub.address.category.dto.CategoryCreateResp;
import org.buratishkin.familyhub.address.category.dto.CategoryDeleteReq;
import org.buratishkin.familyhub.address.category.dto.CategoryUpdateReq;
import org.buratishkin.familyhub.address.port.AddressFamilyAccessPort;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.address.mapper.CategoryMapper;
import org.buratishkin.familyhub.shared.event.DomainEvent;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryManageService {
    private final CategoryCrudService categoryCrudService;
    private final AddressFamilyAccessPort familyAccessPort;
    private final CategoryMapper categoryMapper;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public CreateResp<CategoryCreateResp> create(CategoryCreateReq createReq, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        Long familyId = createReq.familyId();

        CreateResp<CategoryCreateResp> access = validateFamilyAccess(user.id(), familyId, this::getCreateFalseResult);
        if (access != null) {
            return access;
        }

        String name = normalizeName(createReq.name());
        if (categoryCrudService.existsYet(name)) {
            return getCreateFalseResult();
        }

        if (!familyAccessPort.familyExists(familyId)) {
            return getCreateFalseResult();
        }
        CategoryEntity category = categoryMapper.toEntity(createReq, familyId);
        CategoryEntity savedCategory = categoryCrudService.save(category);
        safePublish(new CategoryCreatedEvent(
                savedCategory.getId(),
                familyId,
                user.id(),
                savedCategory.getName(),
                LocalDateTime.now()
        ));
        return getCreateTrueResult(new CategoryCreateResp(true, "good name", savedCategory.getId()));
    }

    @Transactional
    public UpdateResp update(CategoryUpdateReq updateReq, Long categoryId, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        Long familyId = updateReq.familyId();

        UpdateResp access = validateFamilyAccess(user.id(), familyId, this::getUpdateFalseResult);
        if (access != null) {
            return access;
        }

        CategoryEntity category = categoryCrudService.findById(categoryId);
        if (!isCategoryValidForFamily(category, familyId)) {
            return getUpdateFalseResult();
        }

        String name = normalizeName(updateReq.name());
        if (categoryCrudService.existsYet(name) && !Objects.equals(category.getName(), name)) {
            return getUpdateFalseResult();
        }

        category.setName(name);
        categoryCrudService.save(category);
        safePublish(new CategoryUpdatedEvent(
                category.getId(),
                familyId,
                user.id(),
                category.getName(),
                LocalDateTime.now()
        ));
        return getUpdateTrueResult();
    }

    @Transactional
    public DeleteResp delete(CategoryDeleteReq deleteReq, Long categoryId, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!categoryCrudService.existsById(categoryId)) {
            return getDeleteInvalidCategoryResult();
        }

        CategoryEntity category = categoryCrudService.findById(categoryId);
        Long familyId = category.getOwnerFamilyId();
        if (familyId == null) {
            return getDeleteSystemCategoryResult();
        }
        if (deleteReq != null && deleteReq.familyId() != null && !Objects.equals(deleteReq.familyId(), familyId)) {
            return getDeleteFalseResult();
        }

        DeleteResp access = validateFamilyAccess(user.id(), familyId, this::getDeleteFalseResult);
        if (access != null) {
            return access;
        }

        if (!isCategoryValidForFamily(category, familyId)) {
            return getDeleteFalseResult();
        }

        category.setArchived(true);
        categoryCrudService.save(category);
        safePublish(new CategoryArchivedEvent(
                category.getId(),
                familyId,
                user.id(),
                category.getName(),
                LocalDateTime.now()
        ));
        return getDeleteTrueResult();
    }

    private void safePublish(DomainEvent event) {
        try {
            domainEventPublisher.publish(event);
        } catch (RuntimeException e) {
            log.warn("Failed to publish category domain event {}", event.getClass().getName(), e);
        }
    }

    private boolean isCategoryValidForFamily(CategoryEntity category, Long familyId) {
        return Objects.equals(category.getOwnerFamilyId(), familyId);
    }

    private String normalizeName(String name) {
        return name.trim().toLowerCase();
    }

    private <T> T validateFamilyAccess(Long userId, Long familyId, Supplier<T> falseResultSupplier) {
        if (!familyAccessPort.hasFamilyAccess(userId, familyId)) {
            return falseResultSupplier.get();
        }
        return null;
    }

    private CreateResp<CategoryCreateResp> getCreateFalseResult() {
        return new CreateResp<>(
                false,
                "invalid familyId",
                null
        );
    }

    private CreateResp<CategoryCreateResp> getCreateTrueResult(CategoryCreateResp createResp) {
        return new CreateResp<>(
                true,
                "good data",
                createResp
        );
    }

    private UpdateResp getUpdateFalseResult() {
        return new UpdateResp(
                false,
                "invalid familyId"
        );
    }

    private UpdateResp getUpdateTrueResult() {
        return new UpdateResp(
                true,
                "good data"
        );
    }

    private DeleteResp getDeleteFalseResult() {
        return new DeleteResp(
                false,
                "invalid familyId"
        );
    }

    private DeleteResp getDeleteInvalidCategoryResult() {
        return new DeleteResp(
                false,
                "invalid categoryId"
        );
    }

    private DeleteResp getDeleteSystemCategoryResult() {
        return new DeleteResp(
                false,
                "system category cannot be deleted"
        );
    }

    private DeleteResp getDeleteTrueResult() {
        return new DeleteResp(
                true,
                "good data"
        );
    }
}
