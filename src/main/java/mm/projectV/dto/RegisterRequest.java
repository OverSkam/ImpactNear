package mm.projectV.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import mm.projectV.validation.FullUpdate;
import mm.projectV.validation.PartialUpdate;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Name is required", groups = {FullUpdate.class})
    @Length(min = 1, max = 30,
            message = "Name should be at least 1 characters and maximum 30 characters",
            groups = {FullUpdate.class, PartialUpdate.class}
    )
    private String name;

    @NotBlank(message = "Surname is required", groups = {FullUpdate.class})
    @Length(min = 1, max = 30,
            message = "Surname should be at least 1 characters and maximum 30 characters",
            groups = {FullUpdate.class, PartialUpdate.class}
    )
    private String surname;

    @NotBlank(message = "Email is required", groups = {FullUpdate.class})
    @Email(message = "Email should be valid", groups = {FullUpdate.class, PartialUpdate.class})
    private String email;

    @NotBlank(message = "Password is required")
    @Length(min = 8, max = 200,
            message = "Password should be at least 8 characters"
    )
    private String password;
}
