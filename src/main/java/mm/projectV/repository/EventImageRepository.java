package mm.projectV.repository;

import mm.projectV.model.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventImageRepository extends JpaRepository<EventImage, Long> {
    List<EventImage> findByEventIdOrderByDisplayOrderAsc(Long eventId);
    long countByEventId(Long eventId);
    Optional<EventImage> findByIdAndEventId(Long id, Long eventId);
}
