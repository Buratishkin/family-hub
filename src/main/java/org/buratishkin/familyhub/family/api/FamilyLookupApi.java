package org.buratishkin.familyhub.family.api;

public interface FamilyLookupApi {
    FamilyView findFamilyById(Long id);

    boolean existsById(Long familyId);
}
