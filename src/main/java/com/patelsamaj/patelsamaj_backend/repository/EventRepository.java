package com.patelsamaj.patelsamaj_backend.repository;

import com.patelsamaj.patelsamaj_backend.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    @Query("""
        SELECT e
        FROM Event e
        WHERE e.statusId = :statusId
        AND (
            :statusId IN (3, 4)
            OR e.startDate >= CURRENT_DATE
        )
        AND (
            :search IS NULL
            OR :search = ''
            OR LOWER(e.familyHeadName) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Page<Event> findByStatusIdAndSearch(
            @Param("statusId") Integer statusId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
        SELECT e
        FROM Event e
        WHERE e.statusId IN (2, 3)
        AND (
            e.startDate >= CURRENT_DATE
            OR (e.endDate IS NOT NULL AND e.endDate >= CURRENT_DATE)
        )
        """)
    java.util.List<Event> findFutureEventsByStatusIds();
}
