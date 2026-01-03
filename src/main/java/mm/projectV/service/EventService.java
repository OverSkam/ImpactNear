package mm.projectV.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.EventRequest;
import mm.projectV.dto.EventResponse;
import mm.projectV.exception.NotFoundException;
import mm.projectV.facade.EventFacade;
import mm.projectV.model.Event;
import mm.projectV.model.User;
import mm.projectV.repository.EventRepository;
import mm.projectV.util.SortingUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventFacade eventFacade;

    public Page<EventResponse> getAllOwnedEvents(User user, int page, int size, String sortBy, String sortDirection) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);
        return eventRepository.findByUserId(user.getId(), PageRequest.of(page, size, sort))
                .map(eventFacade::toResponse);
    }

    public Page<EventResponse> getRecommendedEvents(int page, int size, String sortBy, String sortDirection) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);
        return eventRepository.findAll(PageRequest.of(page, size, sort))
                .map(eventFacade::toResponse);
    }

    public EventResponse getEvent(User user, Long eventId) {
        Event event = fetchOrThrow(user.getId(), eventId);
        log.info("Event with id: {} was retrieved successfully", eventId);
        return eventFacade.toResponse(event);
    }

    public void createEvent(User user, EventRequest createRequest) {
        Event event = eventFacade.toEntity(createRequest);
        event.setUser(user);
        eventRepository.save(event);
        log.info("New task has been created by user with email: {}", user.getEmail());
    }

    public void updateEvent(User user, Long eventId, EventRequest eventRequest) {
        Event event = fetchOrThrow(user.getId(), eventId);
        event.setName(eventRequest.getName());
        event.setDescription(eventRequest.getDescription());
        event.setStatus(eventRequest.getStatus());
        eventRepository.save(event);
        log.info("Event with id: {} was updated successfully", eventId);
    }

    public void deleteEvent(User user, Long eventId) {
        Event event = fetchOrThrow(user.getId(), eventId);
        eventRepository.delete(event);
        log.info("Event with id: {} was deleted successfully", eventId);
    }

    private Event fetchOrThrow(Long userId, Long eventId) {
        return eventRepository.findByUserIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Event not found exception"));
    }
}
