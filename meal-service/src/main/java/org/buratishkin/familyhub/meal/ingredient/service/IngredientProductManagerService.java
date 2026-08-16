package org.buratishkin.familyhub.meal.ingredient.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.meal.food.FoodCategory;
import org.buratishkin.familyhub.meal.ingredient.IngredientProductEntity;
import org.buratishkin.familyhub.meal.ingredient.dto.IngredientProductCreateReq;
import org.buratishkin.familyhub.meal.ingredient.dto.IngredientProductCreateResp;
import org.buratishkin.familyhub.meal.ingredient.dto.IngredientProductResp;
import org.buratishkin.familyhub.meal.port.MealFamilyAccessPort;
import org.buratishkin.familyhub.meal.projection.MealMemberProjectionRepository;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientProductManagerService {
    private final IngredientProductCrudService ingredientProductCrudService;
    private final IngredientProductMapper ingredientProductMapper;
    private final MealFamilyAccessPort familyAccessPort;
    private final MealMemberProjectionRepository memberProjectionRepository;

    @Transactional(readOnly = true)
    public List<IngredientProductResp> search(Long familyId, String query, Integer limit, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!familyAccessPort.hasFamilyAccess(user.id(), familyId)) {
            return List.of();
        }

        return ingredientProductCrudService.findAvailable(familyId, query, limit).stream()
                .map(ingredientProductMapper::toResp)
                .toList();
    }

    @Transactional
    public CreateResp<IngredientProductCreateResp> create(IngredientProductCreateReq req, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!familyAccessPort.hasFamilyAccess(user.id(), req.familyId())) {
            return new CreateResp<>(false, "invalid familyId", null);
        }
        if (!FoodCategory.exists(req.categoryId())) {
            return new CreateResp<>(false, "invalid categoryId", null);
        }

        String normalizedName = ingredientProductCrudService.normalize(req.name());
        if (ingredientProductCrudService.findExisting(req.familyId(), normalizedName, req.categoryId()).isPresent()) {
            return new CreateResp<>(false, "ingredient already exists", null);
        }
        Long memberId = memberProjectionRepository.findByFamilyIdAndUserId(req.familyId(), user.id())
                .map(member -> member.getMemberId())
                .orElse(null);
        return createNew(req, user.id(), memberId);
    }

    private CreateResp<IngredientProductCreateResp> createNew(IngredientProductCreateReq req, Long userId, Long memberId) {
        IngredientProductEntity product = new IngredientProductEntity();
        product.setId(ingredientProductCrudService.nextCustomId());
        product.setFamilyId(req.familyId());
        product.setCreatedByUserId(userId);
        product.setCreatedByMemberId(memberId);
        product.setName(req.name().trim());
        product.setCategory(FoodCategory.byId(req.categoryId()).displayName());
        product.setCategoryId(req.categoryId());
        product.setBuiltIn(false);
        product.setCreatedAt(LocalDateTime.now());
        ingredientProductCrudService.save(product);
        return new CreateResp<>(true, "good data", new IngredientProductCreateResp(product.getId()));
    }
}
