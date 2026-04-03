package com.memoize.api.Repository;

import com.memoize.api.Entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("""
        SELECT e FROM Event e
        WHERE e.owner.id = :userId
    """)
    List<Event> findEventsForUser(UUID userId);

    @Modifying
    int deleteByIdAndOwnerId(UUID eventId, UUID userId);
}
