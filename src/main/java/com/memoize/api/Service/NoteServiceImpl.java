package com.memoize.api.Service;

import com.memoize.api.Dto.NoteDto;
import com.memoize.api.Dto.NoteModifyRequest;
import com.memoize.api.Entity.Note;
import com.memoize.api.Entity.User;
import com.memoize.api.Repository.NoteRepository;
import com.memoize.api.Repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public NoteDto createNoteByUser(NoteModifyRequest request, UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("No user found");
        }
        User currentUser = entityManager.getReference(User.class, userId);
        Note newNote = Note.builder()
                .content(request.content())
                .preview(request.preview())
                .owner(currentUser)
                .build();
        noteRepository.save(newNote);
        return NoteDto.fromEntity(newNote);
    }

    @Override
    @Transactional
    public NoteDto updateNoteByUser(NoteModifyRequest request, UUID noteId, UUID userId) {
        Note existingNote = noteRepository.findByIdAndOwnerId(noteId, userId).orElseThrow(() -> new EntityNotFoundException("No note found with the given id"));
        existingNote.setContent(request.content());
        existingNote.setPreview(request.preview());
        noteRepository.save(existingNote);
        return NoteDto.fromEntity(existingNote);
    }

    @Override
    public Page<NoteDto> fetchNotesByUser(UUID userId, boolean isDeleted, Pageable pageable) {
        var notesPage = noteRepository.findNotes(userId, isDeleted, pageable);
        return notesPage.map(NoteDto::fromEntity);
    }

    @Override
    @Transactional
    public NoteDto deleteNoteByUser(UUID noteId, UUID userId) {
        Note existingNote = noteRepository.findByIdAndOwnerId(noteId, userId)
                .orElseThrow(() -> new EntityNotFoundException(("Note doesn't exist")));
        if (existingNote.getIsDeleted()) {
            throw new IllegalStateException("Note is already deleted");
        }
        existingNote.setIsDeleted(true);
        existingNote.setDeletedAt(LocalDateTime.now());
        noteRepository.save(existingNote);
        return NoteDto.fromEntity(existingNote);
    }

    @Override
    @Transactional
    public NoteDto restoreNoteByUser(UUID noteId, UUID userId) {
        Note existingNote = noteRepository.findByIdAndOwnerId(noteId, userId)
                .orElseThrow(() -> new EntityNotFoundException(("Note doesn't exist")));
        if (!existingNote.getIsDeleted()) {
            throw new IllegalStateException("Note is already active!");
        }
        existingNote.setIsDeleted(false);
        existingNote.setDeletedAt(null);
        noteRepository.save(existingNote);
        return NoteDto.fromEntity(existingNote);
    }
}
