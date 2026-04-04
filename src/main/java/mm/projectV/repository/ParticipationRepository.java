package mm.projectV.repository;

import mm.projectV.model.Participation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Optional<Participation> findById(Long id);
}
