package com.memoize.api.Service;

import com.memoize.api.Entity.Note;

import java.util.UUID;

public interface NoteService {
    Note createNote(Note note, UUID userId);
}
