package com.memoize.api.Service;

import com.memoize.api.Dto.AiChatDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface AiChatService {
    Page<AiChatDto> getLlmChatsOfUser(UUID userId, Pageable pageable);
    String llmChat(String query, UUID userId);
    Flux<String> llmChatStream(String query, UUID userId);
}
