package mm.projectV.service;

import lombok.AllArgsConstructor;
import mm.projectV.model.Event;
import mm.projectV.model.User;
import mm.projectV.repository.EventRepository;
import mm.projectV.repository.UserRepository;
import mm.projectV.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ResponseEntity<?> createEvent(Event createRequest, long id) {
        createRequest.setUser(userRepository.findById(id).get());
        eventRepository.save(createRequest);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Event was created",
                null
        );
    }
}
