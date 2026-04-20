package mm.projectV.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.*;
import mm.projectV.enums.Role;
import mm.projectV.model.CustomUserDetails;
import mm.projectV.service.AuthService;
import mm.projectV.service.CustomUserDetailsService;
import mm.projectV.util.JwtUtil;
import mm.projectV.handler.ResponseHandler;
import mm.projectV.validation.FullUpdate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated LoginRequest loginRequest) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

        log.info("Logging in user");

        if (!((CustomUserDetails) userDetails).getUser().getEnabled())
            throw new DisabledException("Please verify your email");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDetails.getUsername(), loginRequest.getPassword())
        );
        String jwtToken = jwtUtil.generateToken(userDetails.getUsername());
        Role role = ((CustomUserDetails) userDetails).getUser().getRole();
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User logged in successfully",
                new LoginResponse(jwtToken, role)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Validated(FullUpdate.class) RegisterRequest registerRequest) {
        if (authService.userByEmailExists(registerRequest.getEmail())) {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "An account with this email already exists. If you haven't verified yet, request a new verification email",
                    null
            );
        }

        authService.register(registerRequest);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User has been registered",
                null
        );
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) {
        log.info("Trying to verify user's email");
        authService.verify(token);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User has been verified",
                null
        );
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody EmailRequest emailRequest) {
        log.info("User is trying to get new verification email");
        authService.resendVerification(emailRequest);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "If the email exists and is unverified, a verification link has been sent",
                null
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> requestPasswordReset(@RequestBody EmailRequest emailRequest) {
        log.info("User is requesting password reset for account with email: {}", emailRequest.getEmail());
        authService.requestPasswordReset(emailRequest);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "If an account exists for this email, a password reset link has been sent.",
                null
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest passwordResetRequest) {
        log.info("User is trying to verify password reset");
        authService.resetPassword(passwordResetRequest);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Password was changed successfully",
                null
        );
    }
}
