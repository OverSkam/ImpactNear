package mm.projectV.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.exception.NotAuthenticatedException;
import mm.projectV.model.CustomUserDetails;
import mm.projectV.model.Event;
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

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody Event createRequest
    ) {
        User user = principal.getUser();
        log.info("Creating event for user with email: {}", user.getEmail());
        eventService.createEvent(user, createRequest);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Event was created",
                null
        );
    }
}
