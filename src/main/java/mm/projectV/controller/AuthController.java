package mm.projectV.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.LoginRequest;
import mm.projectV.dto.LoginResponse;
import mm.projectV.dto.RegisterRequest;
import mm.projectV.enums.Role;
import mm.projectV.model.CustomUserDetails;
import mm.projectV.service.AuthService;
import mm.projectV.service.CustomUserDetailsService;
import mm.projectV.util.JwtUtil;
import mm.projectV.util.ResponseHandler;
import mm.projectV.validation.FullUpdate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        UserDetails userDetails = userDetailsService.loadUserByEmail(loginRequest.getEmail());

        log.info("Logging in user");

//        if (!authService.isValidPassword(loginRequest.getPassword(), userDetails.getPassword())) {
//            return ResponseHandler.generateResponse(
//                    HttpStatus.UNAUTHORIZED,
//                    true,
//                    "Invalid password",
//                    null
//            );
//        }

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
                    HttpStatus.valueOf(409),
                    true,
                    "User already exists",
                    null
            );
        }

        authService.register(registerRequest);
//        authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword())
//        );
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User has been registered",
                null
        );
    }
}
