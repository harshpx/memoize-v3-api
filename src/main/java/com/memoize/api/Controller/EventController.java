package com.memoize.api.Controller;

import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

public interface EventController {
    ResponseEntity<CommonResponse<List<EventDto>>> fetchEventsByUser(AuthPrincipal principal);
    ResponseEntity<CommonResponse<List<EventsByDate>>> getUpcomingEventsOfUser(int upcomingDays, AuthPrincipal principal);
    ResponseEntity<CommonResponse<List<EventsByDate>>> getEventsByMonthOfUser(int month, @Min(1990) int year, AuthPrincipal principal);
    ResponseEntity<CommonResponse<EventDto>> createEventByUser(EventModifyRequest request, AuthPrincipal principal);
    ResponseEntity<CommonResponse<EventDto>> updateEventByUser(EventModifyRequest request, UUID eventId, AuthPrincipal principal);
    ResponseEntity<CommonResponse<Integer>> deleteEventByUser(UUID eventId, AuthPrincipal principal);
}
