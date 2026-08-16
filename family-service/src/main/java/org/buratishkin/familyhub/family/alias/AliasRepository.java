package org.buratishkin.familyhub.family.alias;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AliasRepository extends JpaRepository<AliasEntity, Long> {
    List<AliasEntity> findAllByOwnerPerson_Family_Id(Long familyId);
}
