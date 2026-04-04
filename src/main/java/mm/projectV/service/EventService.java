package mm.projectV.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.EventRequest;
import mm.projectV.dto.EventResponse;
import mm.projectV.dto.ParticipationRequest;
import mm.projectV.enums.ParticipationStatus;
import mm.projectV.exception.JoinException;
import mm.projectV.exception.NotFoundException;
import mm.projectV.exception.PermissionException;
import mm.projectV.facade.EventFacade;
import mm.projectV.model.Event;
import mm.projectV.model.Participation;
import mm.projectV.model.User;
import mm.projectV.repository.EventRepository;
import mm.projectV.repository.ParticipationRepository;
import mm.projectV.util.SortingUtil;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
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
    private final ParticipationRepository participationRepository;

    public Page<EventResponse> getAllOrganizedEvents(User user, int page, int size, String sortBy, String sortDirection) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);
        return eventRepository.findByUserId(user.getId(), PageRequest.of(page, size, sort))
                .map(eventFacade::toResponse);
    }

    public Page<EventResponse> getRandomEvents(int page, int size, String sortBy, String sortDirection) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);
        return eventRepository.findAll(PageRequest.of(page, size, sort))
                .map(eventFacade::toResponse);
    }

    public Page<EventResponse> getRecommendedEvents(
            User user, double radius, int page, int size, String sortBy, String sortDirection
    ) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);
        if (user.getLocation() != null) {
            log.info("User with id: {} is getting recommended events", user.getId());
            return eventRepository.findWithinRadius(user.getLocation(), radius, PageRequest.of(page, size, sort))
                    .map(eventFacade::toResponse);
        }
        else {
            log.error("User with id: {} has no location to proceed", user.getId());
            throw new RuntimeException("User doesn't have a location");
        }
    }

    public EventResponse getEvent(User user, Long eventId) {
        Event event = fetchOrThrow(user.getId(), eventId);
        log.info("Event with id: {} was retrieved successfully", eventId);
        return eventFacade.toResponse(event);
    }

    public void createEvent(User user, EventRequest createRequest) {
        Event event = eventFacade.toEntity(createRequest);
        event.setUser(user);
        event.setLocation(createPoint(createRequest.getLongitude(), createRequest.getLatitude()));
        eventRepository.save(event);
        log.info("New event has been created by user with id: {}", user.getId());
    }

    public void updateEvent(User user, Long eventId, EventRequest eventRequest) {
        Event event = fetchOrThrow(user.getId(), eventId);
        event.setName(eventRequest.getName());
        event.setDescription(eventRequest.getDescription());
        event.setStatus(eventRequest.getStatus());
        event.setAddress(eventRequest.getAddress());
        event.setLocation(createPoint(eventRequest.getLongitude(), eventRequest.getLatitude()));
        event.setStartDate(eventRequest.getStartDate());
        event.setEndDate(eventRequest.getEndDate());
        event.setParticipantsCapacity(eventRequest.getParticipantsCapacity());
        event.setStatus(eventRequest.getStatus());
        event.setIsOpen(eventRequest.getIsOpen());
        eventRepository.save(event);
        log.info("Event with id: {} was updated successfully", eventId);
    }

    public void deleteEvent(User user, Long eventId) {
        Event event = fetchOrThrow(user.getId(), eventId);
        eventRepository.delete(event);
        log.info("Event with id: {} was deleted successfully", eventId);
    }

    public void createParticipationRequest(User user, Long eventId, ParticipationRequest participationRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found exception"));
        if (event.getUser().equals(user)) {
            log.warn("User is trying to join his own event");
            throw new JoinException("User can not join his own event");
        }
        Participation participation = new Participation(user, event, ParticipationStatus.PENDING, participationRequest.getMessage(), "");
        participationRepository.save(participation);
        log.info("Participation request was created");
    }

    public void respondToParticipationRequest(User user, Long eventId, Long participationId, ParticipationRequest participationRequest) {
        // TODO: notify on approve/reject
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new NotFoundException("Participation not found"));

        Event event = eventRepository.findById(eventId)
                        .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getUser().getId() != user.getId())
            throw new PermissionException("Permission denied");

        participation.setApprovalResponse(participationRequest.getMessage());
        participation.setStatus(participationRequest.getStatus());
        participationRepository.save(participation);
        log.info("Participation was changed");
    }

    private Event fetchOrThrow(Long userId, Long eventId) {
        return eventRepository.findByUserIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Event not found exception"));
    }

    private Point createPoint(double longitude, double latitude) {
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        return factory.createPoint(new Coordinate(longitude, latitude));
    }
}
