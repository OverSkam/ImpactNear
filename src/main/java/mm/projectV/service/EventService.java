package mm.projectV.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.model.Event;
import mm.projectV.model.User;
import mm.projectV.repository.EventRepository;
import mm.projectV.repository.UserRepository;
import mm.projectV.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public void createEvent(User user, Event createRequest) {
        //noinspection OptionalGetWithoutIsPresent
        createRequest.setUser(userRepository.findById(user.getId()).get());
        eventRepository.save(createRequest);
        log.info("New task has been created by user with email: {}", user.getEmail());
    }


}
