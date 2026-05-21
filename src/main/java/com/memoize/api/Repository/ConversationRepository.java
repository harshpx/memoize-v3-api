package com.memoize.api.Repository;

import com.memoize.api.Dto.ConversationDto;
import com.memoize.api.Entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    @Query("""
        select new com.memoize.api.Dto.ConversationDto(c.id, c.user.id, c.name, c.summary, c.isProperName, c.createdAt, c.updatedAt)
        from Conversation c where c.user.id = :userId
    """)
    List<ConversationDto> fetchConversationsOfUser(UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    Optional<Conversation> findByIdAndUserId(UUID id, UUID userId);
}
