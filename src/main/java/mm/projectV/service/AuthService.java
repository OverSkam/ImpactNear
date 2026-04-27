package mm.projectV.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.PasswordResetRequest;
import mm.projectV.dto.RegisterRequest;
import mm.projectV.dto.EmailRequest;
import mm.projectV.enums.Role;
import mm.projectV.enums.TokenType;
import mm.projectV.exception.InvalidRequestException;
import mm.projectV.model.User;
import mm.projectV.model.VerificationToken;
import mm.projectV.repository.UserRepository;
import mm.projectV.repository.VerificationTokenRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final TokenVersionService tokenVersionService;

    @Transactional
    public void register(RegisterRequest registerRequest) {
        User user = modelMapper.map(registerRequest, User.class);
        user.setEnabled(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
        log.info("User with id: {} was registered", user.getId());

        VerificationToken token = createNewTokenForUser(user, TokenType.EMAIL_VERIFICATION);
        tokenRepository.save(token);
        log.info("Verification token for user with id: {} was created", user.getId());

        emailService.sendVerificationEmail(user.getEmail(), token.getToken());

    }

    @Transactional
    public void verify(String token) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndType(token, TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new InvalidRequestException("Invalid token"));

        if (verificationToken.getTokenExpiresAt().isBefore(LocalDateTime.now()))
            throw new InvalidRequestException("Token expired");

        verificationToken.getUser().setEnabled(true);
        tokenRepository.delete(verificationToken);
        log.info("User with id: {} was verified", verificationToken.getUser().getId());
    }

    public boolean userByEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public void resendVerification(EmailRequest emailRequest) {
        String email = emailRequest.getEmail();
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && !user.get().getEnabled()) {
            log.info("Unverified user with id: {} requesting new verification email", user.get().getId());
            VerificationToken verificationToken = createNewTokenForUser(user.get(), TokenType.EMAIL_VERIFICATION);

            emailService.sendVerificationEmail(user.get().getEmail(), verificationToken.getToken());
        } else
            log.info("User doesn't exist or already verified");
    }

    @Transactional
    public void requestPasswordReset(EmailRequest emailRequest) {
        String email = emailRequest.getEmail();
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            log.info("User with id: {} trying to reset his password", user.get().getId());
            VerificationToken verificationToken = createNewTokenForUser(user.get(), TokenType.PASSWORD_RESET);

            emailService.sendPasswordResetEmail(user.get().getEmail(), verificationToken.getToken());
        } else
            log.info("User doesn't exist");
    }

    @Transactional
    public void resetPassword(PasswordResetRequest passwordResetRequest) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndType(passwordResetRequest.getToken(), TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new InvalidRequestException("Invalid token"));

        if (verificationToken.getTokenExpiresAt().isBefore(LocalDateTime.now()))
            throw new InvalidRequestException("Token expired");

        User user = verificationToken.getUser();
        user.setPassword(passwordEncoder.encode(passwordResetRequest.getPassword()));
        tokenRepository.delete(verificationToken);
        tokenVersionService.bumpVersion(user.getId());
        log.info("User with id: {} has changed his password", user.getId());
    }

    private VerificationToken createNewTokenForUser(User user, TokenType type) {
        VerificationToken currentToken = tokenRepository.findByUserAndType(user, type).orElse(new VerificationToken());
        currentToken.setToken(UUID.randomUUID().toString());
        currentToken.setTokenExpiresAt(LocalDateTime.now().plusMinutes(30));
        currentToken.setUser(user);
        currentToken.setType(type);
        tokenRepository.save(currentToken);
        return currentToken;
    }
}
