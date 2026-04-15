package mm.projectV.repository;

import mm.projectV.model.Participation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Optional<Participation> findById(Long id);
    Page<Participation> findByUserId(Long userId, Pageable pageable);
    Page<Participation> findByEventId(Long eventId, Pageable pageable);
    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT p FROM Participation p WHERE p.event.user.id = :userId")
    Page<Participation> findByEventUserId(@Param("userId") Long userId, Pageable pageable);
}
