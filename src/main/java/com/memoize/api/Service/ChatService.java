package com.memoize.api.Service;

import com.memoize.api.Dto.ChatDto;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    List<ChatDto> getChatsOfConversation(UUID conversationId, UUID userId);
    Flux<String> queryLlmStream(String query, UUID conversationId, UUID userId);
}
