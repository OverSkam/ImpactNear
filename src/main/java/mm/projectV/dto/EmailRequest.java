package mm.projectV.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import mm.projectV.validation.FullUpdate;
import mm.projectV.validation.PartialUpdate;

@Data
@AllArgsConstructor
public class EmailRequest {
    @NotBlank(message = "Email is required", groups = {FullUpdate.class})
    @Email(message = "Email should be valid", groups = {FullUpdate.class, PartialUpdate.class})
    private String email;
}
