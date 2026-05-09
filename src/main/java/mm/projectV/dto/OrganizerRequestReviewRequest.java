package mm.projectV.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import mm.projectV.enums.RequestStatus;

@Data
@AllArgsConstructor
public class OrganizerRequestReviewRequest {
    private String reviewMessage;
    private RequestStatus reviewStatus;
}
