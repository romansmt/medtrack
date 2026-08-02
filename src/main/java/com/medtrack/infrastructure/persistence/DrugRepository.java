package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.Drug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DrugRepository extends JpaRepository<Drug, Long> {

    Optional<Drug> findByPzn(String pzn);

    @Query("SELECT d FROM Drug d WHERE "
            + "LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(d.activeSubstance) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(d.pzn) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Drug> search(@Param("query") String query);
}
