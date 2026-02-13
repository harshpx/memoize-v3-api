package com.memoize.api.Service;

import com.memoize.api.Dto.NoteDto;
import com.memoize.api.Dto.NoteModifyRequest;
import com.memoize.api.Entity.Note;

import java.util.List;
import java.util.UUID;

public interface NoteService {
    NoteDto createNote(NoteModifyRequest request, UUID userId);
    NoteDto updateNote(NoteModifyRequest request, UUID noteId, UUID userId);
    List<NoteDto> fetchNotesByUser(UUID userId);
    void deleteNoteByUser(UUID noteID, UUID userId);
}
