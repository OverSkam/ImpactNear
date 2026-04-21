package mm.projectV.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.projectV.validation.FullUpdate;
import mm.projectV.validation.PartialUpdate;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequest {
    @NotBlank(message = "Name is required", groups = {FullUpdate.class})
    @Length(min = 1, max = 40,
            message = "Name should be at least 1 characters and maximum 40 characters",
            groups = {FullUpdate.class, PartialUpdate.class}
    )
    private String name;

    @NotBlank(message = "Surname is required", groups = {FullUpdate.class})
    @Length(min = 1, max = 40,
            message = "Surname should be at least 1 characters and maximum 40 characters",
            groups = {FullUpdate.class, PartialUpdate.class}
    )
    private String surname;

    private String password;
}
