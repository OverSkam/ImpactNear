package mm.projectV.repository;

import mm.projectV.model.Event;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findById(Long id);
    Page<Event> findByUserId(Long id, Pageable pageable);
    Page<Event> findAll(Pageable pageable);
    List<Event> findAll();
    Optional<Event> findByUserIdAndId(Long userId, Long id);

    @Query("SELECT e FROM Event e WHERE function('ST_Distance_Sphere', e.location, :center) <= :radius")
    Page<Event> findWithinRadius(
            @Param("center") Point center,
            @Param("radius") double radiusInMeters,
            Pageable pageable
    );
}
