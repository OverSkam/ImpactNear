package mm.projectV.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "user_identities",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_identities_provider_subject",
                columnNames = {"provider", "provider_subject"}
        )
)
public class UserIdentity extends AbstractModel {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider", nullable = false, length = 32)
    private String provider;

    @Column(name = "provider_subject", nullable = false)
    private String providerSubject;

    @Column(name = "email_at_link")
    private String emailAtLink;
}