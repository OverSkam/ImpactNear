package mm.projectV.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import mm.projectV.enums.Role;
import org.locationtech.jts.geom.Point;

import java.util.List;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "users")
public class User extends AbstractModel {
    @Column(name="name", nullable = false)
    private String name;

    @Column(name="surname", nullable = false)
    private String surname;

    @Column(name="email", nullable = false, unique = true)
    private String email;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    @Column(name = "token_version", nullable = false)
    private Long tokenVersion = 0L;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    private List<Event> createdEvents;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participation> participations;

    @Column(name = "location", columnDefinition = "POINT SRID 4326")
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;
}
