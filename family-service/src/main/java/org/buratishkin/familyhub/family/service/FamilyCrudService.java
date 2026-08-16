package org.buratishkin.familyhub.family.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.FamilyEntity;
import org.buratishkin.familyhub.family.FamilyRepository;
import org.buratishkin.familyhub.family.api.FamilyLookupApi;
import org.buratishkin.familyhub.family.api.FamilyView;
import org.buratishkin.familyhub.family.exception.FamilyNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FamilyCrudService implements FamilyLookupApi {
    private final FamilyRepository familyRepository;

    public FamilyEntity findById(Long id){
        return familyRepository.findById(id)
                .orElseThrow(() -> new FamilyNotFoundException("Нет семьи с id: " + id));
    }

    public void deleteFamily(Long familyId) {
        FamilyEntity family = findById(familyId);
        deleteFamily(family);
    }

    public void deleteFamily(FamilyEntity family){
        familyRepository.delete(family);
    }

    public FamilyEntity save(FamilyEntity family){
        return familyRepository.save(family);
    }

    public FamilyEntity saveAndFlush(FamilyEntity family){
        return familyRepository.saveAndFlush(family);
    }

    public boolean existsById(Long familyId) {
        return familyRepository.existsById(familyId);
    }

    public FamilyView findFamilyById(Long id) {
        return toView(findById(id));
    }

    private FamilyView toView(FamilyEntity family) {
        if (family == null) {
            return null;
        }
        return new FamilyView(
                family.getId(),
                family.getName(),
                family.getDescription(),
                family.getAdmin() == null ? null : family.getAdmin().getId()
        );
    }
}
