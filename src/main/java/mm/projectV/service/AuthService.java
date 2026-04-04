package mm.projectV.service;

import lombok.AllArgsConstructor;
import mm.projectV.dto.RegisterRequest;
import mm.projectV.enums.Role;
import mm.projectV.model.User;
import mm.projectV.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest registerRequest) {
        User user = modelMapper.map(registerRequest, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
    }

    public boolean userByEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean isValidPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
