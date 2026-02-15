package com.memoize.api.Repository;

import com.memoize.api.Entity.Note;
import com.memoize.api.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    Optional<Note> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<Note> findByOwnerIdOrderByUpdatedAtDesc(UUID id);

    @Modifying
    int deleteByIdAndOwnerId(UUID noteId, UUID userId);

    @Query("""
        SELECT n FROM Note n
        WHERE n.owner.id = :userId AND n.isDeleted = false
        ORDER BY n.updatedAt DESC
    """)
    List<Note> findActiveNotesByUser(UUID userId);

    @Query("""
        SELECT n FROM Note n
        WHERE n.owner.id = :userId AND n.isDeleted = true
        ORDER BY n.deletedAt DESC
    """)
    List<Note> findDeletedNotesByUser(UUID userId);
}
