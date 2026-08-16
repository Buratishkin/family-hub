package org.buratishkin.familyhub.address.category.api;

import java.util.List;

public interface CategoryLookupApi {
    CategoryView findCategoryById(Long id);

    boolean existsById(Long id);

    List<CategoryView> findCategoriesByFamilyId(Long familyId);
}
