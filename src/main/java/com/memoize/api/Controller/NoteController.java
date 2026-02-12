package com.memoize.api.Controller;


import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Dto.NoteModifyRequest;
import com.memoize.api.Entity.Note;
import jakarta.validation.constraints.Null;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface NoteController {
    ResponseEntity<CommonResponse<List<Note>>> fetchAllNotesOfUser(AuthPrincipal principal);
    ResponseEntity<CommonResponse<Note>> createNote(NoteModifyRequest request, AuthPrincipal principal);
    ResponseEntity<CommonResponse<Note>> updateNote(NoteModifyRequest request, UUID noteId, AuthPrincipal principal);
    ResponseEntity<CommonResponse<Void>> deleteNote(UUID noteId, AuthPrincipal principal);
}
