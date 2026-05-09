package mm.projectV.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.enums.RequestStatus;

import java.time.LocalDateTime;

@Slf4j
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "organizer_requests")
public class OrganizerRequest extends AbstractModel {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false)
    private RequestStatus requestStatus;

    @Column(name = "request_message")
    private String requestMessage;

    @Column(name = "review_message")
    private String reviewMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "managed_by_user_id")
    private User managedBy;

    @Column(name = "managed_at")
    private LocalDateTime managedAt;
}
