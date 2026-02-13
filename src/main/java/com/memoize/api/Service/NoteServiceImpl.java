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
import org.springframework.stereotype.Service;

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
    public NoteDto createNote(NoteModifyRequest request, UUID userId) {
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
    public NoteDto updateNote(NoteModifyRequest request, UUID noteId, UUID userId) {
        Note existingNote = noteRepository.findByIdAndOwnerId(noteId, userId).orElseThrow(() -> new EntityNotFoundException("No note found with the given id"));
        existingNote.setContent(request.content());
        existingNote.setPreview(request.preview());
        noteRepository.save(existingNote);
        return NoteDto.fromEntity(existingNote);
    }

    @Override
    public List<NoteDto> fetchNotesByUser(UUID userId) {
        List<Note> notes = noteRepository.findByOwnerId(userId);
        return notes.stream().map(NoteDto::fromEntity).toList();
    }

    @Override
    @Transactional
    public void deleteNoteByUser(UUID noteID, UUID userId) {
        noteRepository.deleteByIdAndOwnerId(noteID, userId);
    }
}
