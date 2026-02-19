package com.memoize.api.Repository;

import com.memoize.api.Entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByEmailAndToken(String email, String token);

    @Query("SELECT COUNT(v) > 0 FROM VerificationToken v WHERE v.email = :email AND v.expiresAt > CURRENT_TIMESTAMP")
    boolean existsValidTokenByEmail(@Param("email") String email);
}
