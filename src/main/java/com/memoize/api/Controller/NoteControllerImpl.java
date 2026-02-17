package com.memoize.api.Controller;

import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Dto.NoteDto;
import com.memoize.api.Dto.NoteModifyRequest;
import com.memoize.api.Entity.Note;
import com.memoize.api.Service.NoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<CommonResponse<Page<NoteDto>>> fetchNotesByUser(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(name = "deleted", defaultValue = "false") boolean isDeleted,
            @PageableDefault(
                    size = 50,
                    sort = "updatedAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        var notes = noteService.fetchNotesByUser(principal.userId(), isDeleted, pageable);
        var response = CommonResponse.success(notes);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping
    public ResponseEntity<CommonResponse<NoteDto>> createNoteByUser(@RequestBody @Valid NoteModifyRequest request, @AuthenticationPrincipal AuthPrincipal principal) {
        var newNote = noteService.createNoteByUser(request, principal.userId());
        var response = CommonResponse.success(newNote);
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<NoteDto>> updateNoteByUser(@RequestBody @Valid NoteModifyRequest request, @PathVariable(name = "id") UUID noteId, @AuthenticationPrincipal AuthPrincipal principal) {
        var updatedNote = noteService.updateNoteByUser(request, noteId, principal.userId());
        var response = CommonResponse.success(updatedNote);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<NoteDto>> deleteNoteByUser(@PathVariable(name = "id") UUID noteId, @AuthenticationPrincipal AuthPrincipal principal) {
        var updatedNote = noteService.deleteNoteByUser(noteId, principal.userId());
        var response = CommonResponse.success(updatedNote);
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}/restore")
    public ResponseEntity<CommonResponse<NoteDto>> restoreNoteByUser(@PathVariable(name = "id") UUID noteId, @AuthenticationPrincipal AuthPrincipal principal) {
        var updatedNote = noteService.restoreNoteByUser(noteId, principal.userId());
        var response = CommonResponse.success(updatedNote);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<CommonResponse<Integer>> permanentDeleteNoteByUser(@PathVariable(name = "id") UUID noteId, @AuthenticationPrincipal AuthPrincipal principal) {
        int result = noteService.permanentDeleteNoteByUser(noteId, principal.userId());
        var response = CommonResponse.success(result);
        return ResponseEntity.ok(response);
    }
}
