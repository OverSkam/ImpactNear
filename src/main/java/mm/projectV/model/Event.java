package mm.projectV.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import mm.projectV.enums.EventStatus;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "events")
public class Event extends AbstractModel {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(20) DEFAULT 'PAUSED'")
    private EventStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participation> participants;

    @Column(name = "address")
    private String address;

    @Column(name = "location", columnDefinition = "POINT")
    private Point location;

    @Column(name = "start")
    private LocalDateTime startDate;

    @Column(name = "end")
    private LocalDateTime endDate;

    @Column(name = "participants_capacity")
    private Long participantsCapacity;

    @Column(name = "participants_number")
    private Long participantsNumber;

    @Column(name = "is_open")
    private Boolean isOpen;
}
