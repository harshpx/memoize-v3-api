package com.memoize.api.Dto;

import java.util.List;
import java.util.UUID;

public record ConversationChatData(UUID conversation, List<ChatDto> chats) {}
