package mm.projectV.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.enums.Role;
import mm.projectV.exception.InvalidRequestException;
import mm.projectV.model.User;
import mm.projectV.model.UserIdentity;
import mm.projectV.repository.UserIdentityRepository;
import mm.projectV.repository.UserRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserLinker {

    private final UserRepository userRepository;
    private final UserIdentityRepository identityRepository;

    @Transactional
    public User resolveOrCreate(String provider, OidcUser oidc) {
        String subject = oidc.getSubject();
        String email = oidc.getEmail();
        Boolean emailVerified = oidc.getEmailVerified();

        if (email == null || Boolean.FALSE.equals(emailVerified)) {
            throw new InvalidRequestException("Provider did not return a verified email");
        }

        var existing = identityRepository.findByProviderAndProviderSubject(provider, subject);
        if (existing.isPresent()) {
            Long userId = existing.get().getUser().getId();
            return userRepository.findById(userId)
                    .orElseThrow(() -> new InvalidRequestException("Linked user no longer exists"));

        }

        var byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            link(user, provider, subject, email);
            log.info("Linked existing user id={} to provider={}", user.getId(), provider);
            return user;
        }

        User user = new User();
        user.setEmail(email);
        user.setName(firstNonBlank(oidc.getGivenName(), localPart(email)));
        user.setSurname(firstNonBlank(oidc.getFamilyName(), "-"));
        user.setPassword(null);
        user.setEnabled(true);
        user.setRole(Role.ROLE_USER);
        user.setTokenVersion(0L);
        userRepository.save(user);
        link(user, provider, subject, email);
        log.info("Provisioned new user id={} via provider={}", user.getId(), provider);
        return user;
    }

    private void link(User user, String provider, String subject, String email) {
        UserIdentity identity = new UserIdentity();
        identity.setUser(user);
        identity.setProvider(provider);
        identity.setProviderSubject(subject);
        identity.setEmailAtLink(email);
        identityRepository.save(identity);
    }

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }

    private static String localPart(String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}