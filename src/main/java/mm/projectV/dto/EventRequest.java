package mm.projectV.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.projectV.enums.EventStatus;
import org.hibernate.validator.constraints.Length;

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

    private EventStatus status;
    private Long userId;
}
