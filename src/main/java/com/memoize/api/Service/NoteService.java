package com.memoize.api.Service;

import com.memoize.api.Dto.NoteDto;
import com.memoize.api.Dto.NoteModifyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NoteService {
    NoteDto createNoteByUser(NoteModifyRequest request, UUID userId);

    NoteDto updateNoteByUser(NoteModifyRequest request, UUID noteId, UUID userId);

    Page<NoteDto> fetchNotesByUser(UUID userId, boolean isDeleted, Pageable pageable);

    NoteDto deleteNoteByUser(UUID noteID, UUID userId);

    NoteDto restoreNoteByUser(UUID noteID, UUID userId);

    int permanentDeleteNoteByUser(UUID noteID, UUID userId);
}
