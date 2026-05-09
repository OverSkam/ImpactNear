package mm.projectV.repository;

import mm.projectV.model.OrganizerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizerRequestRepository extends JpaRepository<OrganizerRequest, Long> {
    Optional<OrganizerRequest> findById(Long id);
    Page<OrganizerRequest> findAll(Pageable pageable);
    boolean existsByUserId(Long userId);
}
