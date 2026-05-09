package mm.projectV.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.OrganizerRequestReviewRequest;
import mm.projectV.handler.ResponseHandler;
import mm.projectV.model.CustomUserDetails;
import mm.projectV.model.User;
import mm.projectV.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    @GetMapping()
    public ResponseEntity<?> getOrganizerRequests(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "create_date") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        User user = principal.getUser();
        log.info("Fetching organizer requests for admin with id: {}", user.getId());
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Organizer requests was fetched successfully",
                adminService.getOrganizerRequests(page, size, sortBy, sortDirection)
        );
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<?> getOrganizerRequest(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long requestId
    ) {
        User user = principal.getUser();
        log.info("Fetching organizer request with id: {} for admin with id: {}", requestId, user.getId());
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Organizer requests was fetched successfully",
                adminService.getOrganizerRequest(requestId)
        );
    }

    @PutMapping("/respond/{requestId}")
    public ResponseEntity<?> respondToOrganizerRequest(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long requestId,
            @RequestBody OrganizerRequestReviewRequest reviewRequest
            ) {
        User user = principal.getUser();
        log.info("Admin with id: {} responding to request with id: {}", user.getId(), requestId);
        adminService.respondToOrganizerRequest(user, requestId, reviewRequest);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Organizer request was managed successfully",
                null
        );
    }
}
