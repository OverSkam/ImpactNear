package mm.projectV.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.projectV.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    private Long id;
    private String name;
    private String address;
    private String description;
    private EventStatus status;
    private Long userId;
    private double longitude;
    private double latitude;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long participantsCapacity;
    private Long participantsNumber;
    private List<EventImageResponse> images;
}
