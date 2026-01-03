package mm.projectV.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.projectV.enums.EventStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    private String name;
    private String description;
    private EventStatus status;
    private Long userId;
}
