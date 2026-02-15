package com.memoize.api.Service;

import com.memoize.api.Dto.NoteDto;
import com.memoize.api.Dto.NoteModifyRequest;
import com.memoize.api.Entity.Note;

import java.util.List;
import java.util.UUID;

public interface NoteService {
    NoteDto createNoteByUser(NoteModifyRequest request, UUID userId);
    NoteDto updateNoteByUser(NoteModifyRequest request, UUID noteId, UUID userId);
    List<NoteDto> fetchActiveNotesByUser(UUID userId);
    List<NoteDto> fetchDeletedNotesByUser(UUID userId);
    NoteDto deleteNoteByUser(UUID noteID, UUID userId);
    NoteDto restoreNoteByUser(UUID noteID, UUID userId);
}
