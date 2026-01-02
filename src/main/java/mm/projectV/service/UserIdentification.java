package mm.projectV.service;

import lombok.AllArgsConstructor;
import mm.projectV.exception.NotFoundException;
import mm.projectV.model.User;
import mm.projectV.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserIdentification {
    private final UserRepository userRepository;

    public User identify(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found in database"));
    }
}
