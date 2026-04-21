package mm.projectV.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.LocationRequest;
import mm.projectV.dto.ProfileUpdateRequest;
import mm.projectV.mapper.UserMapper;
import mm.projectV.model.CustomUserDetails;
import mm.projectV.model.User;
import mm.projectV.service.UserService;
import mm.projectV.handler.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<?>  getUser(@PathVariable Long userId) {
        log.info("Organizer trying to get information about user with id: {}", userId);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User fetched successfully",
                userService.getUser(userId)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = principal.getUser();
        log.info("Fetching profile for user with id: {}", user.getId());
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "My profile",
                userMapper.toResponse(user)
        );
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Validated @RequestBody ProfileUpdateRequest profileRequest
    ) {
        User user = principal.getUser();
        log.info("Updating profile for user with id: {}", user.getId());
        userService.updateProfile(user, profileRequest);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Users profile was updated successfully",
                null
        );
    }

    @PatchMapping("/me")
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
