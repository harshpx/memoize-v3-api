package com.memoize.api.Service;

import com.memoize.api.Dto.AiChatDto;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface AiChatService {
    List<AiChatDto> getLlmChatsOfUser(UUID userId);
    String llmChat(String query, UUID userId);
    Flux<String> llmChatStream(String query, UUID userId);
}
