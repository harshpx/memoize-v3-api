package com.memoize.api.Repository;

import com.memoize.api.Dto.AiChatDto;
import com.memoize.api.Entity.AiChat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AiChatRepository extends JpaRepository<AiChat, UUID> {
    @Query("""
        select new com.memoize.api.Dto.AiChatDto(aic.id, aic.user.id, aic.content, aic.type, aic.createdAt)
        from AiChat aic
        where aic.user.id = :userId
        order by aic.createdAt desc
    """)
    Page<AiChatDto> fetchChatsOfUser(UUID userId, Pageable pageable);
}
