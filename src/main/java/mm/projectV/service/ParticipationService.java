package mm.projectV.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.ParticipationRequest;
import mm.projectV.dto.ParticipationResponse;
import mm.projectV.enums.ParticipationStatus;
import mm.projectV.enums.Role;
import mm.projectV.exception.JoinException;
import mm.projectV.exception.NotFoundException;
import mm.projectV.exception.PermissionException;
import mm.projectV.mapper.ParticipationMapper;
import mm.projectV.model.Event;
import mm.projectV.model.Participation;
import mm.projectV.model.User;
import mm.projectV.repository.EventRepository;
import mm.projectV.repository.ParticipationRepository;
import mm.projectV.util.SortingUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ParticipationService {
    private final ParticipationRepository participationRepository;
    private final ParticipationMapper participationMapper;
    private final EventRepository eventRepository;

    public Page<ParticipationResponse> getMyParticipationRequests(User user, int page, int size, String sortBy, String sortDirection) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);

        return participationRepository.findByUserId(user.getId(), PageRequest.of(page, size, sort))
                .map(participationMapper::toResponse);
    }

    public Page<ParticipationResponse> getIncomingParticipationRequests(User user, int page, int size, String sortBy, String sortDirection) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);
        return participationRepository.findByEventUserId(user.getId(), PageRequest.of(page, size, sort))
                .map(participationMapper::toResponse);
    }

    public Page<ParticipationResponse> getParticipationRequestsRelatedToEvent(User user, Long eventId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);

        if (user.getRole().equals(Role.ROLE_ORGANIZER)) {
            return participationRepository.findByEventId(eventId, PageRequest.of(page, size, sort))
                    .map(participationMapper::toResponse);
        }

        throw new PermissionException("Access to participation requests denied");
    }

    public ParticipationResponse getMyParticipationRequest(User user, Long participationId) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new NotFoundException("Participation request not found"));

        if (user.getId().equals(participation.getUser().getId()))
            return participationMapper.toResponse(participation);

        throw new PermissionException("Access to participation request denied");
    }

    public ParticipationResponse getIncomingParticipationRequest(User user, Long participationId) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new NotFoundException("Participation request not found"));

        if (!eventRepository.existsByIdAndUserId(participation.getEvent().getId(), user.getId()))
            throw new PermissionException("Access to participation request denied");

        return participationMapper.toResponse(participation);

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
}
