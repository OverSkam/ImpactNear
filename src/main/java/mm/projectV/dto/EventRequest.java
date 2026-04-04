package mm.projectV.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.projectV.enums.EventStatus;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest {
    @NotBlank(message = "Event name is required")
    @Length(max = 40,
            message = "Event name should be maximum 40 characters"
    )
    private String name;

    @Length(max = 500,
            message = "Event description should be maximum 500 characters"
    )
    private String description;

    @NotBlank(message = "Event latitude is required")
    private Double latitude;

    @NotBlank(message = "Event longitude is required")
    private Double longitude;

    @NotBlank(message = "Event address is required")
    private String address;

    @NotBlank(message = "Start date is required")
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Long participantsCapacity = 10000L;

    private Boolean isOpen = true;

    private EventStatus status = EventStatus.PLANNED;
    private Long userId;
}
