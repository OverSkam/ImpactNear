package mm.projectV.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.projectV.enums.ParticipationStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequest {
    private String message;
    private ParticipationStatus status;
}
