package mm.projectV.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationRequest {
    @NotBlank(message = "Latitude is required")
    @Min(-90)
    @Max(90)
    private Double latitude;

    @NotBlank(message = "Longitude is required")
    @Min(-180)
    @Max(180)
    private Double longitude;
}
