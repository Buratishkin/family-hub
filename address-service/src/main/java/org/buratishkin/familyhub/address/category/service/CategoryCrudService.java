package org.buratishkin.familyhub.address.category.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.address.category.CategoryEntity;
import org.buratishkin.familyhub.address.category.CategoryRepository;
import org.buratishkin.familyhub.address.category.api.CategoryLookupApi;
import org.buratishkin.familyhub.address.category.api.CategoryView;
import org.buratishkin.familyhub.address.category.exception.CategoryNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryCrudService implements CategoryLookupApi {
    private final CategoryRepository categoryRepository;

    public CategoryEntity findById(Long id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Нет категории с id: " + id));
    }

    public boolean existsById(Long id){
        return categoryRepository.existsById(id);
    }

    public boolean existsYet(String name){
        return categoryRepository.existsByName(name.trim().toLowerCase());
    }

    public CategoryEntity save(CategoryEntity category){
        return categoryRepository.save(category);
    }

    public List<CategoryEntity> findAllByFamilyId(Long familyId) {
        return categoryRepository.findAllAvailableToFamily(familyId);
    }

    public CategoryView findCategoryById(Long id) {
        return toView(findById(id));
    }

    public List<CategoryView> findCategoriesByFamilyId(Long familyId) {
        return findAllByFamilyId(familyId).stream()
                .map(this::toView)
                .toList();
    }

    private CategoryView toView(CategoryEntity category) {
        if (category == null) {
            return null;
        }
        return new CategoryView(
                category.getId(),
                category.getOwnerFamilyId(),
                category.getName(),
                category.getType() == null ? null : category.getType().name(),
                category.isArchived(),
                category.getCreatedAt()
        );
    }
}
