package com.memoize.api.Service;

import com.memoize.api.Dto.NoteModifyRequest;
import com.memoize.api.Entity.Note;

import java.util.List;
import java.util.UUID;

public interface NoteService {
    Note createNote(NoteModifyRequest request, UUID userId);
    Note updateNote(NoteModifyRequest request, UUID noteId, UUID userId);
    List<Note> fetchNotesByUser(UUID userId);
    void deleteNoteByUser(UUID noteID, UUID userId);
}
