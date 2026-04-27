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
    boolean existsByIdAndUserId(Long eventId, Long userId);

    @Query(value = """
          SELECT * FROM events e
          WHERE MBRContains(
              ST_SRID(
                  ST_GeomFromText(CONCAT('POLYGON((',
                      :minLon, ' ', :minLat, ',',
                      :maxLon, ' ', :minLat, ',',
                      :maxLon, ' ', :maxLat, ',',
                      :minLon, ' ', :maxLat, ',',
                      :minLon, ' ', :minLat, '))')),
                  4326),
              e.location)
          AND ST_Distance_Sphere(e.location, :center) <= :radiusMeters
        """,
            countQuery = """
                    SELECT COUNT(*) FROM events e
                    WHERE MBRContains(
                        ST_SRID(
                            ST_GeomFromText(CONCAT('POLYGON((',
                                :minLon, ' ', :minLat, ',',
                                :maxLon, ' ', :minLat, ',',
                                :maxLon, ' ', :maxLat, ',',
                                :minLon, ' ', :maxLat, ',',
                                :minLon, ' ', :minLat, '))')),
                            4326),
                        e.location)
                    AND ST_Distance_Sphere(e.location, :center) <= :radiusMeters
                    """,
            nativeQuery = true)
    Page<Event> findWithinRadius(
            @Param("center") Point center,
            @Param("radiusMeters") double radiusMeters,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon,
            Pageable pageable
    );
}
