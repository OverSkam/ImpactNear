package mm.projectV.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.projectV.enums.RequestStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationResponse {
    private Long id;
    private Long userId;
    private Long eventId;
    private RequestStatus status;
    private String approvalRequest;
    private String approvalResponse;
    private LocalDateTime createDate;
    private LocalDateTime lastModifiedDate;
}
