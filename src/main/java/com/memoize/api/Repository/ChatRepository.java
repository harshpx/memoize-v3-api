package com.memoize.api.Repository;

import com.memoize.api.Dto.ChatDto;
import com.memoize.api.Entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query("""
        select new com.memoize.api.Dto.ChatDto(c.id, c.conversation.id, c.content, c.type, c.createdAt)
        from Chat c where c.conversation.id = :conversationId
    """)
    List<ChatDto> fetchChatsByConversationId(UUID conversationId);
}
