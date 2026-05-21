package com.memoize.api.Service;

import com.memoize.api.Dto.ConversationDto;

import java.util.List;
import java.util.UUID;

public interface ConversationService {
    List<ConversationDto> getConversationsOfUser(UUID userId);
    ConversationDto createConversationForUser(UUID userId);
    ConversationDto getConversationByIdForUser(UUID conversationId, UUID userId);
}
