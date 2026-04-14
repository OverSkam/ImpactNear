package mm.projectV.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.EventRequest;
import mm.projectV.dto.EventResponse;
import mm.projectV.dto.ParticipationRequest;
import mm.projectV.dto.ParticipationResponse;
import mm.projectV.enums.ParticipationStatus;
import mm.projectV.enums.Role;
import mm.projectV.exception.JoinException;
import mm.projectV.exception.LocationException;
import mm.projectV.exception.NotFoundException;
import mm.projectV.exception.PermissionException;
import mm.projectV.facade.EventFacade;
import mm.projectV.facade.ParticipationFacade;
import mm.projectV.model.AbstractModel;
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

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventFacade eventFacade;
    private final ParticipationRepository participationRepository;
    private final ParticipationFacade participationFacade;
    private static final GeometryFactory GEO_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);


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

    public Object getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                        .orElseThrow(() -> new NotFoundException("Event not found"));
        log.info("Event with id: {} was retrieved successfully", eventId);
        return eventFacade.toResponse(event);
    }

    public Page<EventResponse> getRecommendedEvents(
            User user, double radius, int page, int size, String sortBy, String sortDirection
    ) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);
        if (user.getLocation() == null)
            throw new LocationException("User doesn't have a location");

        log.info("User with id: {} is getting recommended events", user.getId());
        log.info("Users coords: {}", user.getLocation());
        return eventRepository.findWithinRadius(user.getLocation(), radius, PageRequest.of(page, size, sort))
                .map(eventFacade::toResponse);
    }

    public EventResponse getOrganizedEvent(User user, Long eventId) {
        Event event = fetchOrThrow(user.getId(), eventId);
        log.info("Organized event with id: {} was retrieved successfully", eventId);
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
        setIfNotNull(event::setName, eventRequest.getName());
        setIfNotNull(event::setDescription, eventRequest.getDescription());
        setIfNotNull(event::setAddress, eventRequest.getAddress());
        setIfNotNull(event::setStartDate, eventRequest.getStartDate());
        setIfNotNull(event::setEndDate, eventRequest.getEndDate());
        setIfNotNull(event::setParticipantsCapacity, eventRequest.getParticipantsCapacity());
        setIfNotNull(event::setStatus, eventRequest.getStatus());
        setIfNotNull(event::setIsOpen, eventRequest.getIsOpen());
        if (eventRequest.getLongitude() != null && eventRequest.getLatitude() != null)
            event.setLocation(createPoint(eventRequest.getLongitude(), eventRequest.getLatitude()));
        eventRepository.save(event);
        log.info("Event with id: {} was updated successfully", eventId);
    }

    public void deleteEvent(User user, Long eventId) {
        Event event = fetchOrThrow(user.getId(), eventId);
        eventRepository.delete(event);
        log.info("Event with id: {} was deleted successfully", eventId);
    }

//    public Page<ParticipationResponse> getParticipationRequests(User user, int page, int size, String sortBy, String sortDirection) {
//        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);
//
//        if (user.getRole().equals(Role.ROLE_USER))
//            return participationRepository.findByUserId(user.getId(), PageRequest.of(page, size, sort))
//                    .map(participationFacade::toResponse);
//
//        List<Long> ids = eventRepository.findByUserId(user.getId()).stream().map(AbstractModel::getId).toList();
//        return participationRepository.findByEventIdIn(ids, PageRequest.of(page, size, sort))
//                .map(participationFacade::toResponse);
//    }

    public Page<ParticipationResponse> getMyParticipationRequests(User user, int page, int size, String sortBy, String sortDirection) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);

        return participationRepository.findByUserId(user.getId(), PageRequest.of(page, size, sort))
                .map(participationFacade::toResponse);
    }

    public Page<ParticipationResponse> getOthersParticipationRequests(User user, int page, int size, String sortBy, String sortDirection) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);
        return participationRepository.findByEventUserId(user.getId(), PageRequest.of(page, size, sort))
                .map(participationFacade::toResponse);
    }

    public Page<ParticipationResponse> getParticipationRequestsRelatedToEvent(User user, Long eventId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);

        if (user.getRole().equals(Role.ROLE_ORGANIZER)) {
            return participationRepository.findByEventId(eventId, PageRequest.of(page, size, sort))
                    .map(participationFacade::toResponse);
        }

        throw new PermissionException("Access to participation requests denied");
    }

//    public ParticipationResponse getParticipationRequest(User user, Long participationId) {
//        Participation participation = participationRepository.findById(participationId)
//                .orElseThrow(() -> new NotFoundException("Participation request not found"));
//
//        if (user.getRole().equals(Role.ROLE_USER))
//            if (user.getId() == participation.getUser().getId())
//                return participationFacade.toResponse(participation);
//
//        if (user.getRole().equals(Role.ROLE_ORGANIZER)) {
//            List<Long> ids = eventRepository.findByUserId(user.getId()).stream().map(Event::getUser).map(User::getId).toList();
//            if (ids.contains(user.getId()))
//                return participationFacade.toResponse(participation);
//        }
//
//        throw new PermissionException("Access to participation request denied");
//    }

    public ParticipationResponse getMyParticipationRequest(User user, Long participationId) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new NotFoundException("Participation request not found"));

        if (user.getId().equals(participation.getUser().getId()))
            return participationFacade.toResponse(participation);

        throw new PermissionException("Access to participation request denied");
    }

    public ParticipationResponse getOthersParticipationRequest(User user, Long participationId) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new NotFoundException("Participation request not found"));

        if (!eventRepository.existsByIdAndUserId(participation.getEvent().getId(), user.getId()))
            throw new PermissionException("Access to participation request denied");

        return participationFacade.toResponse(participation);

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

        Event event = participation.getEvent();

        if (!event.getUser().getId().equals(user.getId()))
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
        return GEO_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    private <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null)
            setter.accept(value);
    }
}
