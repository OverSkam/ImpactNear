package mm.projectV.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import mm.projectV.enums.Role;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Role role;
}
