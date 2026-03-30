package com.memoize.api.Repository;

import com.memoize.api.Entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("""
        SELECT e FROM Event e
        WHERE e.owner.id = :userId
    """)
    Page<Event> findEventsForUser(UUID userId, Pageable pageable);

    @Query("""
        SELECT e from Event e
        WHERE e.owner.id = :userId AND e.start > CURRENT_TIMESTAMP
    """)
    Page<Event> findUpcomingEventsForUser(UUID userId, Pageable pageable);

    @Modifying
    int deleteByIdAndOwnerId(UUID eventId, UUID userId);
}
