package org.buratishkin.familyhub.address.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    boolean existsByName(String name);

    List<CategoryEntity> findAllByOwnerFamilyId(Long familyId);

    @Query("""
            select c
            from CategoryEntity c
            where c.ownerFamilyId = :familyId or c.ownerFamilyId is null
            order by
                case when c.ownerFamilyId is null then 0 else 1 end,
                c.id
            """)
    List<CategoryEntity> findAllAvailableToFamily(@Param("familyId") Long familyId);
}
