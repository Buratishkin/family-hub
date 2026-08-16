package org.buratishkin.familyhub.family.alias.api;

import java.util.List;

public interface AliasLookupApi {
    List<AliasView> findAliasesByFamilyId(Long familyId);
}
