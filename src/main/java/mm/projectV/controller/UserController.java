package mm.projectV.controller;

import lombok.AllArgsConstructor;
import mm.projectV.model.User;
import mm.projectV.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody User userRequest) {
        return userService.createUser(userRequest);
    }
}
