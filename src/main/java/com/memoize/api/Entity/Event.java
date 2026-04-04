package com.memoize.api.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.memoize.api.Enum.EventRepeat;
import com.memoize.api.Enum.EventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "location")
    private String location;

    @Column(name = "start", nullable = false)
    private OffsetDateTime start;

    @Column(name = "end", nullable = false)
    private OffsetDateTime end;

    @Column(name = "event_type", length = 50, nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private EventType eventType = EventType.EVENT;

    @Column(name = "event_repeat", nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private EventRepeat eventRepeat = EventRepeat.NONE;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_events_user")
    )
    @JsonIgnore
    private User owner;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
