package mm.projectV.repository;

import mm.projectV.model.Participation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Optional<Participation> findById(Long id);
    Page<Participation> findByUserId(Long userId, Pageable pageable);
    Page<Participation> findByEventIdIn(List<Long> ids, Pageable pageable);
    Page<Participation> findByEventId(Long eventId, PageRequest of);
}
