package com.linktracker.repository;

import com.linktracker.entity.Influencer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InfluencerRepository extends JpaRepository<Influencer, Long> {

    Optional<Influencer> findByCode(String code);

    boolean existsByCode(String code);

    // Callers always pass a non-null String (empty string as the "no
    // filter" default), so the ":search IS NULL" branch is unnecessary --
    // and removing it also avoids Postgres's "could not determine data
    // type of parameter" error, which is thrown at query-prepare time for
    // any parameter whose only textual occurrence is inside an "IS NULL"
    // check with no other type-establishing context (see the note on
    // ClickEventRepository for more detail).
    @Query("""
            SELECT i FROM Influencer i
            WHERE (:search = '' OR
                   LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(i.code) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Influencer> search(@Param("search") String search, Pageable pageable);
}
