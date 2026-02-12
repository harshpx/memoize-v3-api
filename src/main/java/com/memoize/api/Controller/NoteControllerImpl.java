package com.memoize.api.Controller;

import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Dto.NoteModifyRequest;
import com.memoize.api.Entity.Note;
import com.memoize.api.Service.NoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteControllerImpl implements NoteController {
    private final NoteService noteService;
    @Override
    @GetMapping
    public ResponseEntity<CommonResponse<List<Note>>> fetchAllNotesOfUser(@AuthenticationPrincipal AuthPrincipal principal) {
        var notes = noteService.fetchNotesByUser(principal.userId());
        var response = CommonResponse.success(notes);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping
    public ResponseEntity<CommonResponse<Note>> createNote(@RequestBody @Valid NoteModifyRequest request, @AuthenticationPrincipal AuthPrincipal principal) {
        var newNote = noteService.createNote(request, principal.userId());
        var response = CommonResponse.success(newNote);
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<Note>> updateNote(@RequestBody @Valid NoteModifyRequest request, @PathVariable(name = "id") UUID noteId, @AuthenticationPrincipal AuthPrincipal principal) {
        var updatedNote = noteService.updateNote(request, noteId, principal.userId());
        var response = CommonResponse.success(updatedNote);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteNote(@PathVariable(name = "id") UUID noteId, @AuthenticationPrincipal AuthPrincipal principal) {
        noteService.deleteNoteByUser(noteId, principal.userId());
        var response = CommonResponse.successWithoutData();
        return ResponseEntity.ok(response);
    }
}
