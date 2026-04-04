package mm.projectV.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.LocationRequest;
import mm.projectV.model.CustomUserDetails;
import mm.projectV.model.User;
import mm.projectV.service.UserService;
import mm.projectV.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @PatchMapping()
    public ResponseEntity<?> changeUserLocation(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody LocationRequest locationRequest
    ) {
        User user = principal.getUser();
        log.info("Changing location for user with id: {}", user.getId());
        userService.changeLocation(user, locationRequest);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User's location changed successfully",
                null
        );
    }
}
