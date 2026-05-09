package mm.projectV.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.OrganizerRequestReviewRequest;
import mm.projectV.enums.RequestStatus;
import mm.projectV.enums.Role;
import mm.projectV.exception.NotFoundException;
import mm.projectV.model.OrganizerRequest;
import mm.projectV.model.User;
import mm.projectV.repository.OrganizerRequestRepository;
import mm.projectV.util.SortingUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Slf4j
@Service
@AllArgsConstructor
public class AdminService {
    private final OrganizerRequestRepository organizerRequestRepository;
    private final TokenVersionService tokenVersionService;

    @Transactional(readOnly = true)
    public Page<OrganizerRequest> getOrganizerRequests(int page, int size, String sortBy, String sortDirection) {
        Sort sort = SortingUtil.sortGenerator(sortBy, sortDirection);
        return organizerRequestRepository.findAll(PageRequest.of(page, size, sort));
    }

    @Transactional(readOnly = true)
    public OrganizerRequest getOrganizerRequest(Long requestId) {
        return organizerRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Organizer request not found"));
    }

    @Transactional
    public void respondToOrganizerRequest(User admin, Long requestId, OrganizerRequestReviewRequest reviewRequest) {
        OrganizerRequest organizerRequest = organizerRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Organizer request not found"));

        organizerRequest.setRequestStatus(reviewRequest.getReviewStatus());
        organizerRequest.setReviewMessage(reviewRequest.getReviewMessage());
        organizerRequest.setManagedBy(admin);
        organizerRequest.setManagedAt(LocalDateTime.now());
        User user = organizerRequest.getUser();
        if (reviewRequest.getReviewStatus().equals(RequestStatus.APPROVED)) {
            user.setRole(Role.ROLE_ORGANIZER);
            tokenVersionService.bumpVersion(user.getId());
        }
        organizerRequestRepository.save(organizerRequest);
    }
}
