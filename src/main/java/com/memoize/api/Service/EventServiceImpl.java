package com.memoize.api.Service;

import com.memoize.api.Dto.EventDto;
import com.memoize.api.Dto.EventModifyRequest;
import com.memoize.api.Entity.Event;
import com.memoize.api.Entity.User;
import com.memoize.api.Repository.EventRepository;
import com.memoize.api.Repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public EventDto createEventByUser(EventModifyRequest request, UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("No user found");
        }
        User currentUser = entityManager.getReference(User.class, userId);
        Event newEvent = Event.builder()
                .owner(currentUser)
                .title(request.title())
                .start(request.start())
                .end(request.end())
                .eventType(request.eventType())
                .description(request.description())
                .location(request.location())
                .build();
        eventRepository.save(newEvent);
        return EventDto.fromEntity(newEvent);
    }

    @Override
    @Transactional
    public EventDto updateEventByUser(EventModifyRequest request, UUID eventId, UUID userId) {
        Event existingEvent = eventRepository.findByIdAndOwnerId(eventId, userId).orElseThrow(() -> new EntityNotFoundException("No event found with the given id"));
        existingEvent.setTitle(request.title());
        existingEvent.setStart(request.start());
        existingEvent.setEnd(request.end());
        existingEvent.setEventType(request.eventType());
        if (request.description() != null) {
            existingEvent.setDescription(request.description());
        }
        if (request.location() != null) {
            existingEvent.setLocation(request.location());
        }
        eventRepository.save(existingEvent);
        return EventDto.fromEntity(existingEvent);
    }

    @Override
    public Page<EventDto> fetchEventsByUser(UUID userId, Pageable pageable) {
        Page<Event> eventsPage = eventRepository.findEventsForUser(userId, pageable);
        return eventsPage.map(EventDto::fromEntity);
    }

    @Override
    @Transactional
    public int deleteEventByUser(UUID eventId, UUID userId) {
        int result = eventRepository.deleteByIdAndOwnerId(eventId, userId);
        if (result == 0) {
            throw new IllegalArgumentException("Event doesn't exist or unable to deleted");
        }
        return result;
    }
}
