package com.memoize.api.Controller;

import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Dto.EventDto;
import com.memoize.api.Dto.EventModifyRequest;
import com.memoize.api.Dto.EventsByDate;
import com.memoize.api.Service.EventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventControllerImpl implements EventController {
    private final EventService eventService;

    @Override
    @GetMapping("/all")
    public ResponseEntity<CommonResponse<List<EventDto>>> fetchEventsByUser(@AuthenticationPrincipal AuthPrincipal principal) {
        var response = eventService.fetchEventsByUser(principal.userId());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Override
    @GetMapping("/upcoming")
    public ResponseEntity<CommonResponse<List<EventsByDate>>> getUpcomingEventsOfUser(
            @RequestParam(name = "days") int days,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        if (days < 1) {
            throw new IllegalArgumentException("Days can not be less than 1");
        }
        var response = eventService.getUpcomingEventsOfUser(days, principal.userId());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Override
    @GetMapping("/monthly")
    public ResponseEntity<CommonResponse<List<EventsByDate>>> getEventsByMonthOfUser(
            @RequestParam(name = "month") int month,
            @RequestParam(name = "year") int year,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month provided");
        }
        if (year < 1900 || year > 3000) {
            throw new IllegalArgumentException("Years outside [1900, 3000] range are not supported");
        }
        var response = eventService.getEventsByMonthOfUser(month, year, principal.userId());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Override
    @PostMapping
    public ResponseEntity<CommonResponse<EventDto>> createEventByUser(
            @RequestBody @Valid EventModifyRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        var response = eventService.createEventByUser(request, principal.userId());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Override
    @PutMapping("/{eventId}")
    public ResponseEntity<CommonResponse<EventDto>> updateEventByUser(
            @RequestBody @Valid EventModifyRequest request,
            @PathVariable(name = "eventId") UUID eventId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        var response = eventService.updateEventByUser(request, eventId, principal.userId());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Override
    @DeleteMapping("/{eventId}")
    public ResponseEntity<CommonResponse<Integer>> deleteEventByUser(
            @PathVariable(name = "eventId") UUID eventId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        var response = eventService.deleteEventByUser(eventId, principal.userId());
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
