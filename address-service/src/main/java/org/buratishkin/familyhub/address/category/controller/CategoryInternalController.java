package org.buratishkin.familyhub.address.category.controller;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.address.category.api.CategoryView;
import org.buratishkin.familyhub.address.category.service.CategoryCrudService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/categories")
public class CategoryInternalController {
    private final CategoryCrudService categoryCrudService;

    @GetMapping("/{categoryId}")
    public CategoryView findCategoryById(@PathVariable Long categoryId) {
        return categoryCrudService.findCategoryById(categoryId);
    }

    @GetMapping("/{categoryId}/exists")
    public boolean existsById(@PathVariable Long categoryId) {
        return categoryCrudService.existsById(categoryId);
    }

    @GetMapping("/families/{familyId}")
    public List<CategoryView> findCategoriesByFamilyId(@PathVariable Long familyId) {
        return categoryCrudService.findCategoriesByFamilyId(familyId);
    }
}
