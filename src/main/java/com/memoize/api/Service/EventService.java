package com.memoize.api.Service;

import com.memoize.api.Dto.EventDto;
import com.memoize.api.Dto.EventModifyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {
    EventDto createEventByUser(EventModifyRequest request, UUID userId);
    EventDto updateEventByUser(EventModifyRequest request, UUID eventId, UUID userId);
    Page<EventDto> fetchEventsByUser(UUID userId, Pageable pageable);
    int deleteEventByUser(UUID eventId, UUID userId);
}