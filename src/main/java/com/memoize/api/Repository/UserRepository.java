package com.memoize.api.Repository;

import com.memoize.api.Entity.User;
import com.memoize.api.Enum.AuthSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("""
        SELECT u FROM User u
        WHERE u.username = :identifier OR u.email = :identifier
    """)
    Optional<User> findByIdentifier(@Param("identifier") String identifier);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("""
        select u.authSource from User u where u.email = :email
    """)
    Optional<AuthSource> getAuthSourceByEmail(@Param("email") String email);
}
