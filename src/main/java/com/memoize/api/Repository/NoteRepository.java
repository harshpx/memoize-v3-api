package com.memoize.api.Repository;

import com.memoize.api.Entity.Note;
import com.memoize.api.Entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    Optional<Note> findByIdAndOwner(UUID noteId, User owner);
    Optional<Note> findByIdAndOwnerId(UUID id, UUID ownerId);
    List<Note> findByOwnerId(UUID id);
    @Modifying
    void deleteByIdAndOwnerId(UUID noteId, UUID userId);
}
