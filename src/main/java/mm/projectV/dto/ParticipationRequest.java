package mm.projectV.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.projectV.enums.RequestStatus;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequest {
    @Length(max = 40,
            message = "Message should be less than 40 characters"
    )
    private String message;
    private RequestStatus status;
}
