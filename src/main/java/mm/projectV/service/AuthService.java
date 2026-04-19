package mm.projectV.service;

import lombok.AllArgsConstructor;
import mm.projectV.dto.RegisterRequest;
import mm.projectV.enums.Role;
import mm.projectV.exception.InvalidRequestException;
import mm.projectV.model.User;
import mm.projectV.model.VerificationToken;
import mm.projectV.repository.UserRepository;
import mm.projectV.repository.VerificationTokenRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Transactional
    public void register(RegisterRequest registerRequest) {
        User user = modelMapper.map(registerRequest, User.class);
        user.setEnabled(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);
        VerificationToken token = new VerificationToken();
        userRepository.save(user);

        token.setToken(UUID.randomUUID().toString());
        token.setTokenExpiresAt(LocalDateTime.now().plusMinutes(30));
        token.setUser(user);
        tokenRepository.save(token);

        emailService.sendVerificationEmail(user.getEmail(), token.getToken());

    }

    @Transactional
    public void verify(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRequestException("Invalid token"));

        if (verificationToken.getTokenExpiresAt().isBefore(LocalDateTime.now()))
            throw new InvalidRequestException("Token expired");

        verificationToken.getUser().setEnabled(true);
        tokenRepository.delete(verificationToken);
    }

    public boolean userByEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
