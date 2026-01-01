package mm.projectV.controller;

import lombok.AllArgsConstructor;
import mm.projectV.model.Event;
import mm.projectV.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/events")
public class EventController {
    private final EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestBody Event createRequest, @RequestParam long id) {
        return eventService.createEvent(createRequest, id);
    }
}
