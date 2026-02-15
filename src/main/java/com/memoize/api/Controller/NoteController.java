package com.memoize.api.Controller;


import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Dto.NoteDto;
import com.memoize.api.Dto.NoteModifyRequest;
import com.memoize.api.Entity.Note;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface NoteController {
    ResponseEntity<CommonResponse<List<NoteDto>>> fetchActiveNotesByUser(AuthPrincipal principal);
    ResponseEntity<CommonResponse<List<NoteDto>>> fetchDeletedNotesByUser(AuthPrincipal principal);
    ResponseEntity<CommonResponse<NoteDto>> createNoteByUser(NoteModifyRequest request, AuthPrincipal principal);
    ResponseEntity<CommonResponse<NoteDto>> updateNoteByUser(NoteModifyRequest request, UUID noteId, AuthPrincipal principal);
    ResponseEntity<CommonResponse<NoteDto>> deleteNoteByUser(UUID noteId, AuthPrincipal principal);
    ResponseEntity<CommonResponse<NoteDto>> restoreNoteByUser(UUID noteId, AuthPrincipal principal);
}
