package mm.projectV.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.EventRequest;
import mm.projectV.model.CustomUserDetails;
import mm.projectV.model.User;
import mm.projectV.service.EventService;
import mm.projectV.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/events")
public class EventController {
    private final EventService eventService;

    @GetMapping("/get-owned")
    public ResponseEntity<?> getAllOwnedEvents(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        User user = principal.getUser();
        log.info("Fetching all events for user with id: {}", user.getId());
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User's events fetched successfully",
                eventService.getAllOwnedEvents(user, page, size, sortBy, sortDirection)
        );
    }

    @GetMapping("/get-recommended")
    public ResponseEntity<?> getRecommendedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        log.info("Fetching recommended events for unknown user");
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Recommended events for unknown user fetched successfully",
                eventService.getRecommendedEvents(page, size, sortBy, sortDirection)
        );
    }

    @GetMapping("/get/{eventId}")
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

    @PostMapping("/create")
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

    @PutMapping("/update/{eventId}")
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

    @DeleteMapping("/delete/{eventId}")
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
}
