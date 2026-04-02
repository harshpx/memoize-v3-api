package com.memoize.api.Service;

import com.memoize.api.Dto.EventDto;
import com.memoize.api.Dto.EventModifyRequest;
import com.memoize.api.Dto.EventsByDate;
import com.memoize.api.Dto.Pair;
import com.memoize.api.Entity.Event;
import com.memoize.api.Entity.User;
import com.memoize.api.Enum.EventRepeat;
import com.memoize.api.Repository.EventRepository;
import com.memoize.api.Repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    private static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    @Override
    public List<EventDto> fetchEventsByUser(UUID userId) {
        List<Event> eventsPage = eventRepository.findEventsForUser(userId);
        return eventsPage.stream().map(EventDto::fromEntity).toList();
    }

    @Override
    public List<EventsByDate> getUpcomingEventsOfUser(int upcomingDays, UUID userId) {
        List<EventDto> eventList = fetchEventsByUser(userId);
        var rangeStart = OffsetDateTime.now(ZoneOffset.UTC).with(LocalTime.MIN);
        var rangeEnd = rangeStart.plusDays(upcomingDays).with(LocalTime.MAX);
        return getUpcomingEventsHelper(eventList, Pair.of(rangeStart, rangeEnd));
    }

    @Override
    public List<EventsByDate> getEventsByMonthOfUser(int month, int year, UUID userId) {
        List<EventDto> eventList = fetchEventsByUser(userId);
        return getUpcomingEventsHelper(eventList, getMonthGridStartEnd(month, year));
    }

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
    @Transactional
    public int deleteEventByUser(UUID eventId, UUID userId) {
        int result = eventRepository.deleteByIdAndOwnerId(eventId, userId);
        if (result == 0) {
            throw new IllegalArgumentException("Event doesn't exist or unable to deleted");
        }
        return result;
    }

    // helper methods
    private List<EventsByDate> getUpcomingEventsHelper(List<EventDto> events, Pair<OffsetDateTime, OffsetDateTime> range) {
        var rangeStart = range.getFirst();
        var rangeEnd = range.getSecond();

        Map<String, List<EventDto>> eventsByDateMap = new HashMap<>();
        for (var event : events) {
            var eventStart = event.start();
            var eventEnd = event.end();
            long eventDurationMs = ChronoUnit.MILLIS.between(eventStart, eventEnd);
            if (event.eventRepeat() == EventRepeat.YEARLY) {
                for (int y = rangeStart.getYear() - 1; y <= rangeEnd.getYear(); y++) {
                    var start = eventStart.withYear(y);
                    var end = start.plus(eventDurationMs, ChronoUnit.MILLIS);
                    var interval = Pair.of(start, end);
                    if (!start.isAfter(rangeEnd) && !end.isBefore(rangeStart)) {
                        pushDaysToEventMap(interval, range, event, eventsByDateMap);
                    }
                }
            } else if (event.eventRepeat() == EventRepeat.MONTHLY) {
                var curr = rangeStart.with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1);
                while (!curr.isAfter(rangeEnd)) {
                    var daysInMonth = YearMonth.from(curr).lengthOfMonth();
                    var start = OffsetDateTime.of(
                            curr.getYear(), curr.getMonthValue(), Math.min(eventStart.getDayOfMonth(), daysInMonth),
                            eventStart.getHour(), eventStart.getMinute(), eventStart.getSecond(), eventStart.getNano(), ZoneOffset.UTC);
                    var end = start.plus(eventDurationMs, ChronoUnit.MILLIS);
                    var interval = Pair.of(start, end);
                    if (!start.isAfter(rangeEnd) && !end.isBefore(rangeStart)) {
                        pushDaysToEventMap(interval, range, event, eventsByDateMap);
                    }
                    curr = curr.plusMonths(1);
                }
            } else if (event.eventRepeat() == EventRepeat.WEEKLY) {
                var curr = rangeStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                curr = curr.minusWeeks(1);
                while (!curr.isAfter(rangeEnd)) {
                    var start = curr.plusDays(eventStart.getDayOfWeek().getValue() % 7)
                            .withHour(eventStart.getHour())
                            .withMinute(eventStart.getMinute())
                            .withSecond(eventStart.getSecond())
                            .withNano(eventStart.getNano());

                    var end = start.plus(eventDurationMs, ChronoUnit.MILLIS);
                    var interval = Pair.of(start, end);
                    if (!start.isAfter(rangeEnd) && !end.isBefore(rangeStart)) {
                        pushDaysToEventMap(interval, range, event, eventsByDateMap);
                    }
                    curr = curr.plusWeeks(1);
                }
            } else {
                var interval = Pair.of(eventStart, eventEnd);
                if (!eventStart.isAfter(rangeEnd) && !eventEnd.isBefore(rangeStart)) {
                    pushDaysToEventMap(interval, range, event, eventsByDateMap);
                }
            }
        }
        List<EventsByDate> eventsByDateList = new ArrayList<>();
        var itr = rangeStart;
        while (!itr.isAfter(rangeEnd)) {
            var key = itr.format(STANDARD_FORMATTER);
            var eventList = eventsByDateMap.get(key);
            eventsByDateList.add(new EventsByDate(key, eventList != null ? eventList : new ArrayList<>()));
            itr = itr.plusDays(1);
        }
        return eventsByDateList;
    }

    private void pushDaysToEventMap(Pair<OffsetDateTime, OffsetDateTime> interval, Pair<OffsetDateTime, OffsetDateTime> range, EventDto event, Map<String, List<EventDto>> eventsByDate) {
        var clampedStart = interval.getFirst().isBefore(range.getFirst()) ? range.getFirst() : interval.getFirst();
        var clampedEnd = interval.getSecond().isAfter(range.getSecond()) ? range.getSecond() : interval.getSecond();
        var currDay = clampedStart.with(LocalTime.MIN);
        while (!currDay.isAfter(clampedEnd)) {
            var key = currDay.format(STANDARD_FORMATTER);
            eventsByDate.putIfAbsent(key, new ArrayList<>());
            eventsByDate.get(key).add(event);
            currDay = currDay.plusDays(1);
        }
    }

    private Pair<OffsetDateTime, OffsetDateTime> getMonthGridStartEnd(int month, int year) {
        var st = OffsetDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        var en = st.with(TemporalAdjusters.lastDayOfMonth());
        var monthGridStart = st.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MIN);
        var monthGridEnd = en.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).with(LocalTime.MAX);
        return Pair.of(monthGridStart, monthGridEnd);
    }
}
