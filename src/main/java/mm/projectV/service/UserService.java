package mm.projectV.service;

import lombok.AllArgsConstructor;
import mm.projectV.model.User;
import mm.projectV.repository.UserRepository;
import mm.projectV.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public ResponseEntity<?> createUser(User userRequest) {
        userRepository.save(userRequest);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User created",
                null
        );
    }
}
