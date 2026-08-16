package org.buratishkin.familyhub.meal.ingredient.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.meal.ingredient.IngredientProductEntity;
import org.buratishkin.familyhub.meal.ingredient.IngredientProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IngredientProductCrudService {
    private static final int MAX_SEARCH_LIMIT = 100;

    private final IngredientProductRepository repository;

    public IngredientProductEntity save(IngredientProductEntity product) {
        product.setNormalizedName(normalize(product.getName()));
        return repository.save(product);
    }

    public Optional<IngredientProductEntity> findExisting(Long familyId, String normalizedName) {
        Optional<IngredientProductEntity> familyProduct = repository.findByFamilyIdAndNormalizedName(familyId, normalizedName);
        if (familyProduct.isPresent()) {
            return familyProduct;
        }
        return repository.findByBuiltInTrueAndNormalizedName(normalizedName);
    }

    public Optional<IngredientProductEntity> findExisting(Long familyId, String normalizedName, Long categoryId) {
        Optional<IngredientProductEntity> familyProduct = repository.findByFamilyIdAndNormalizedNameAndCategoryId(familyId, normalizedName, categoryId);
        if (familyProduct.isPresent()) {
            return familyProduct;
        }
        return repository.findByBuiltInTrueAndNormalizedNameAndCategoryId(normalizedName, categoryId);
    }

    public IngredientProductEntity findAvailableById(Long productId, Long familyId) {
        IngredientProductEntity product = repository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("invalid ingredientId"));
        if (product.isBuiltIn() || familyId.equals(product.getFamilyId())) {
            return product;
        }
        throw new IllegalArgumentException("invalid ingredientId");
    }

    public List<IngredientProductEntity> findAvailable(Long familyId, String query, Integer limit) {
        int actualLimit = limit == null ? 50 : Math.max(1, Math.min(limit, MAX_SEARCH_LIMIT));
        String actualQuery = query == null || query.isBlank() ? null : query.trim();
        return repository.findAvailable(familyId, actualQuery, PageRequest.of(0, actualLimit));
    }

    public List<IngredientProductEntity> findAllAvailable(Long familyId) {
        return repository.findAllAvailable(familyId);
    }

    public boolean builtInExists(String normalizedName) {
        return repository.existsByBuiltInTrueAndNormalizedName(normalizedName);
    }

    @Transactional
    public Long nextCustomId() {
        long nextId = 1L;
        for (Long id : repository.findAllIdsOrderByIdAsc()) {
            if (id == null || id < nextId) {
                continue;
            }
            if (id > nextId) {
                return nextId;
            }
            nextId++;
        }
        return nextId;
    }

    public String normalize(String value) {
        return Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFKC);
    }
}
