package com.memoize.api.Entity;

import com.memoize.api.Enum.AuthSource;
import com.memoize.api.Enum.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_id", columnList = "id"),
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email")
})
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid default gen_random_uuid()", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "auth_source", length = 20, nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private AuthSource authSource = AuthSource.EMAIL;

    @Column(name = "password")
    @ToString.Exclude
    private String password;

    @Column(name = "avatar_url")
    @Builder.Default
    private String avatarUrl = "https://i.imgur.com/8GO2mo5.png";

    @Column(name = "role", nullable = false, length = 10)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @ToString.Exclude
    private Set<Note> notes = new HashSet<>();

    // for user details
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @PrePersist
    public void prePersist() {
        if (this.role == null) {
            this.role = Role.USER;
        }
        if (this.authSource == null) {
            this.authSource = AuthSource.EMAIL;
        }
        if (this.avatarUrl == null || this.avatarUrl.isBlank()) {
            this.avatarUrl = "https://i.imgur.com/8GO2mo5.png";
        }
    }
}
