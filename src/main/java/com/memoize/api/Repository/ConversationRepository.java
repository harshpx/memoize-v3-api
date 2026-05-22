package com.memoize.api.Repository;

import com.memoize.api.Dto.ConversationDto;
import com.memoize.api.Entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    @Query("""
        select new com.memoize.api.Dto.ConversationDto(
            c.id, c.user.id, c.name, c.summary, c.isProperName, c.isNew, c.createdAt, c.updatedAt
        )
        from Conversation c where c.user.id = :userId order by c.updatedAt desc
    """)
    List<ConversationDto> fetchConversationsOfUser(UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    Optional<Conversation> findByIdAndUserId(UUID id, UUID userId);

    @Query("select count(c) > 0 from Conversation c where c.isNew = true and c.user.id = :userId")
    boolean newConversationExistsForUser(@Param("userId") UUID userId);

    @Query("select c.summary from Conversation c where c.id = :id")
    Optional<String> findSummaryById(@Param("id") UUID id);

    @Modifying
    int deleteByIdAndUserId(UUID id, UUID userId);
}
