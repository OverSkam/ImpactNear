package mm.projectV.repository;

import mm.projectV.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUserId(Long id);
    Page<Event> findByUserId(Long id, Pageable pageable);
    Page<Event> findAll(Pageable pageable);
    Optional<Event> findByUserIdAndId(Long userId, Long id);
}
