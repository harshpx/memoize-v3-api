package com.memoize.api.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "notes", indexes = {
        @Index(name = "idx_note_id", columnList = "id")
})
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid default gen_random_uuid()", updatable = false, nullable = false)
    private UUID id;

    @NotEmpty
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotEmpty
    @Column(name = "preview", columnDefinition = "TEXT")
    private String preview;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_archived", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isArchived = false;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User owner;

    @PrePersist
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
        this.isArchived = false;
        this.deletedAt = null;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
