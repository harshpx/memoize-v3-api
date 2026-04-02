package com.memoize.api.Service;

import com.memoize.api.Dto.EventDto;
import com.memoize.api.Dto.EventModifyRequest;
import com.memoize.api.Dto.EventsByDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EventService {
    List<EventDto> fetchEventsByUser(UUID userId);
    List<EventsByDate> getUpcomingEventsOfUser(int upcomingDays, UUID userId);
    List<EventsByDate> getEventsByMonthOfUser(int month, int year, UUID userId);
    EventDto createEventByUser(EventModifyRequest request, UUID userId);
    EventDto updateEventByUser(EventModifyRequest request, UUID eventId, UUID userId);
    int deleteEventByUser(UUID eventId, UUID userId);
}