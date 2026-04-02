package com.memoize.api.DevTesting;

import com.memoize.api.Dto.EventDto;
import com.memoize.api.Dto.Pair;
import com.memoize.api.Entity.Event;
import com.memoize.api.Enum.EventRepeat;
import com.memoize.api.Repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TestService {
    private final EventRepository eventRepository;

    private static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    public Map<String, Object> testMethod1(int month, int year) {
        List<Event> events = eventRepository.findAll();
        List<EventDto> eventList = events.stream().map(EventDto::fromEntity).toList();

        var range = getMonthGridStartEnd(month, year);
        var rangeStart = range.getFirst(); var rangeEnd = range.getSecond();

        Map<String, List<EventDto>> eventsByDate = new HashMap<>();
        List<Pair<String, List<EventDto>>> eventsByDateList = new ArrayList<>();

        var itr = OffsetDateTime.from(rangeStart).with(LocalTime.MIN);
        while (!itr.isAfter(rangeEnd)) {
            eventsByDateList.add(Pair.of(itr.format(STANDARD_FORMATTER), new ArrayList<>()));
            itr = itr.plusDays(1);
        }

        for (var event : eventList) {
            var eventStart = event.start();
            var eventEnd = event.end();
            long eventDurationMs = ChronoUnit.MILLIS.between(eventStart, eventEnd);
            if (event.eventRepeat() == EventRepeat.YEARLY) {
                for (int y = rangeStart.getYear() - 1; y <= rangeEnd.getYear(); y++) {
                    var start = eventStart.withYear(y);
                    var end = start.plus(eventDurationMs, ChronoUnit.MILLIS);
                    if (!start.isAfter(rangeEnd) && !end.isBefore(rangeStart)) {
                        pushDays(start, end, range, event, eventsByDate);
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
                    if (!start.isAfter(rangeEnd) && !end.isBefore(rangeStart)) {
                        pushDays(start, end, range, event, eventsByDate);
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
                    if (!start.isAfter(rangeEnd) && !end.isBefore(rangeStart)) {
                        pushDays(start, end, range, event, eventsByDate);
                    }
                    curr = curr.plusWeeks(1);
                }
            } else {
                if (!eventStart.isAfter(rangeEnd) && !eventEnd.isBefore(rangeStart)) {
                    pushDays(eventStart, eventEnd, range, event, eventsByDate);
                }
            }
        }
        for (var pair : eventsByDateList) {
            var key = pair.getFirst();
            eventsByDate.putIfAbsent(key, new ArrayList<>());
            pair.setSecond(eventsByDate.get(key));
        }
        return Map.of("EventsByDate", eventsByDateList);
    }

    private void pushDays(OffsetDateTime start, OffsetDateTime end, Pair<OffsetDateTime, OffsetDateTime> range, EventDto event, Map<String, List<EventDto>> eventsByDate) {
        var clampedStart = start.isBefore(range.getFirst()) ? range.getFirst() : start;
        var clampedEnd = end.isAfter(range.getSecond()) ? range.getSecond() : end;
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
