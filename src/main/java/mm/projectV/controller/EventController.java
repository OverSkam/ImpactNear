package mm.projectV.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.EventRequest;
import mm.projectV.dto.ParticipationRequest;
import mm.projectV.dto.ParticipationResponse;
import mm.projectV.model.CustomUserDetails;
import mm.projectV.model.User;
import mm.projectV.service.EventService;
import mm.projectV.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/events")
public class EventController {
    private final EventService eventService;

    @GetMapping("/organized-events")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<?> getAllOrganizedEvents(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        User user = principal.getUser();
        log.info("Fetching all events for user with id: {}", user.getId());
        log.warn("User with role: {}", user.getRole());
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User's events fetched successfully",
                eventService.getAllOrganizedEvents(user, page, size, sortBy, sortDirection)
        );
    }

    @GetMapping("/random-events")
    public ResponseEntity<?> getRandomEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        log.info("Fetching random events for unknown user");
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Recommended events for unknown user fetched successfully",
                eventService.getRandomEvents(page, size, sortBy, sortDirection)
        );
    }

    @GetMapping("/recommended-events")
    public ResponseEntity<?> getRecommendedEvents(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "10000.0") double radius
    ) {
        User user = principal.getUser();
        log.info("Fetching recommended events for user with id: {}", user.getId());
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Recommended events fetched successfully",
                eventService.getRecommendedEvents(user, radius, page, size, sortBy, sortDirection)
        );
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEvent(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long eventId
    ) {
        User user = principal.getUser();
        log.info("Fetching event with id: {} for user with id: {}", eventId, user.getId());
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Event was fetched successfully",
                eventService.getEvent(user, eventId)
        );
    }

    @PostMapping()
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<?> createEvent(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody EventRequest createRequest
    ) {
        User user = principal.getUser();
        log.info("Creating event for user with id: {}", user.getId());
        eventService.createEvent(user, createRequest);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Event was created",
                null
        );
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<?> updateEvent(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long eventId,
            @RequestBody EventRequest eventRequest
    ) {
        User user = principal.getUser();
        log.info("Updating event with id: {} for user with id: {}", eventId, user.getId());
        eventService.updateEvent(user, eventId, eventRequest);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Event was updated successfully",
                null
        );
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<?> deleteEvent(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long eventId
    ) {
        User user = principal.getUser();
        log.info("Deleting event with id: {} for user with id: {}", eventId, user.getId());
        eventService.deleteEvent(user, eventId);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Event was deleted successfully",
                null
        );
    }

    @PostMapping("/{eventId}/participation-requests")
    public ResponseEntity<?> createParticipationRequest(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long eventId,
            @RequestBody ParticipationRequest participationRequest
    ) {
        User user = principal.getUser();
        log.info("Creating participation request for user with id: {}", user.getId());
        eventService.createParticipationRequest(user, eventId, participationRequest);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Participation request was created successfully",
                null
        );
    }

    @PutMapping("/{eventId}/participation-requests/{participationId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<?> respondToParticipationRequest(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long eventId,
            @PathVariable Long participationId,
            @RequestBody ParticipationRequest participationRequest
    ) {
        User user = principal.getUser();
        log.info("Responding to participation request with id: {}", participationId);
        eventService.respondToParticipationRequest(user, eventId, participationId, participationRequest);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Participation request was created successfully",
                null
        );
    }
}
