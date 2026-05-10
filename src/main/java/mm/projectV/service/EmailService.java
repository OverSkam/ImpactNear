package mm.projectV.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final Resend resend;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.verification.base-url}")
    private String verifyBaseUrl;

    @Value("${app.password-reset.base-url}")
    private String resetPasswordUrl;

    public void sendVerificationEmail(String to, String token) {
        String link = verifyBaseUrl + "?token=" + token;
        send(to, "Verify your account",
                "<p>Click to verify your account:</p>"
                        + "<p><a href=\"" + link + "\">" + link + "</a></p>"
                        + "<p>This link expires in 30 minutes.</p>");
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = resetPasswordUrl + "?token=" + token;
        send(to, "Password reset",
                "<p>Click to reset your password:</p>"
                        + "<p><a href=\"" + link + "\">" + link + "</a></p>"
                        + "<p>If you did not request this, ignore this email.</p>");
    }

    private void send(String to, String subject, String html) {
        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(from)
                .to(to)
                .subject(subject)
                .html(html)
                .build();
        try {
            resend.emails().send(options);
        } catch (ResendException e) {
            log.error("Resend send failed to={} subject={}", to, subject, e);
            throw new IllegalStateException("Failed to send email", e);
        }
    }
}