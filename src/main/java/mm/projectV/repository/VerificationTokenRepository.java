package mm.projectV.repository;

import mm.projectV.enums.TokenType;
import mm.projectV.model.User;
import mm.projectV.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndType(String token, TokenType type);
    Optional<VerificationToken> findByUserAndType(User user, TokenType type);
}
