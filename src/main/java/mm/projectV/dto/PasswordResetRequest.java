package mm.projectV.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
public class PasswordResetRequest {
    @NotBlank(message = "Password is required")
    @Length(min = 8, max = 200,
            message = "Password should be at least 8 characters"
    )
    private String password;

    @NotBlank
    private String token;
}
