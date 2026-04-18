package mm.projectV.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.EventRequest;
import mm.projectV.dto.EventResponse;
import mm.projectV.exception.LocationException;
import mm.projectV.exception.NotFoundException;
import mm.projectV.mapper.EventMapper;
import mm.projectV.mapper.ParticipationMapper;
import mm.projectV.model.Event;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private static final GeometryFactory GEO_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional(readOnly = true)
    public Page<EventResponse> getAllOrganizedEvents(User user, int page, int size, String sortBy, String sortDirection) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);
        return eventRepository.findByUserId(user.getId(), PageRequest.of(page, size, sort))
                .map(eventMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getRandomEvents(int page, int size, String sortBy, String sortDirection) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);
        return eventRepository.findAll(PageRequest.of(page, size, sort))
                .map(eventMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                        .orElseThrow(() -> new NotFoundException("Event not found"));
        log.info("Event with id: {} was retrieved successfully", eventId);
        return eventMapper.toResponse(event);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getRecommendedEvents(
            User user, double radius, int page, int size, String sortBy, String sortDirection
    ) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);
        if (user.getLocation() == null)
            throw new LocationException("User doesn't have a location");

        log.info("User with id: {} is getting recommended events", user.getId());
        return eventRepository.findWithinRadius(user.getLocation(), radius, PageRequest.of(page, size, sort))
                .map(eventMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EventResponse getOrganizedEvent(User user, Long eventId) {
        Event event = fetchOrThrow(user.getId(), eventId);
        log.info("Organized event with id: {} was retrieved successfully", eventId);
        return eventMapper.toResponse(event);
    }

    @Transactional
    public void createEvent(User user, EventRequest createRequest) {
        Event event = eventMapper.toEntity(createRequest);
        event.setUser(user);
        event.setParticipantsNumber(0L);
        event.setLocation(createPoint(createRequest.getLongitude(), createRequest.getLatitude()));
        eventRepository.save(event);
        log.info("New event has been created by user with id: {}", user.getId());
    }

    @Transactional
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

    @Transactional
    public void deleteEvent(User user, Long eventId) {
        Event event = fetchOrThrow(user.getId(), eventId);
        eventRepository.delete(event);
        log.info("Event with id: {} was deleted successfully", eventId);
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
