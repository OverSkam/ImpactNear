package mm.projectV.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.model.User;
import mm.projectV.service.OAuth2UserLinker;
import mm.projectV.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2UserLinker linker;
    private final JwtUtil jwtUtil;

    @Value("${app.oauth2.success-redirect}")
    private String successRedirect;

    @Value("${app.oauth2.failure-redirect}")
    private String failureRedirect;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            String provider = token.getAuthorizedClientRegistrationId();
            OidcUser oidc = (OidcUser) token.getPrincipal();

            User user = linker.resolveOrCreate(provider, oidc);
            String jwt = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getTokenVersion());

            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            String target = successRedirect
                    + "#token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8)
                    + "&role="  + URLEncoder.encode(user.getRole().name(), StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response, target);
        } catch (Exception e) {
            log.warn("OAuth2 login success handler failed", e);
            String target = failureRedirect + "?error=" + URLEncoder.encode("oauth_failed", StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response, target);
        }
    }
}
