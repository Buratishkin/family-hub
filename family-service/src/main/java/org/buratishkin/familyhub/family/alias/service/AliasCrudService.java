package org.buratishkin.familyhub.family.alias.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.alias.AliasEntity;
import org.buratishkin.familyhub.family.alias.AliasRepository;
import org.buratishkin.familyhub.family.alias.api.AliasLookupApi;
import org.buratishkin.familyhub.family.alias.api.AliasView;
import org.buratishkin.familyhub.family.alias.exception.AliasNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AliasCrudService implements AliasLookupApi {
    private final AliasRepository aliasRepository;

    public AliasEntity findById(Long id){
        return aliasRepository.findById(id)
                .orElseThrow(() -> new AliasNotFoundException("Нет прозвища с id: " + id));
    }

    public void deleteById(Long id){
        aliasRepository.deleteById(id);
    }

    public void delete(AliasEntity alias){
        aliasRepository.delete(alias);
    }

    public AliasEntity save(AliasEntity alias){
        return aliasRepository.save(alias);
    }

    public List<AliasEntity> findAllByFamilyId(Long familyId) {
        return aliasRepository.findAllByOwnerPerson_Family_Id(familyId);
    }

    public List<AliasView> findAliasesByFamilyId(Long familyId) {
        return findAllByFamilyId(familyId).stream()
                .map(this::toView)
                .toList();
    }

    private AliasView toView(AliasEntity alias) {
        if (alias == null) {
            return null;
        }
        return new AliasView(
                alias.getId(),
                alias.getOwnerPerson() == null ? null : alias.getOwnerPerson().getId(),
                alias.getTargetPerson() == null ? null : alias.getTargetPerson().getId(),
                alias.getAlias()
        );
    }
}
