package mm.projectV.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.projectV.enums.EventStatus;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest {
    @NotBlank(message = "Task name is required")
    @Length(max = 40,
            message = "Task name should be maximum 40 characters"
    )
    private String name;

    @Length(max = 500,
            message = "Task description should be maximum 500 characters"
    )
    private String description;

    private EventStatus status;
    private Long userId;
}
