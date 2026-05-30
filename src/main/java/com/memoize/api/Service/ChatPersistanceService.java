package com.memoize.api.Service;

import com.memoize.api.Entity.Chat;
import com.memoize.api.Entity.Conversation;
import com.memoize.api.Enum.ChatType;
import com.memoize.api.Repository.ChatRepository;
import com.memoize.api.Repository.ConversationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class ChatPersistanceService {
    private final ConversationRepository conversationRepository;
    private final ChatRepository chatRepository;

    public ChatPersistanceService(ConversationRepository conversationRepository,
                                  ChatRepository chatRepository) {
        this.conversationRepository = conversationRepository;
        this.chatRepository = chatRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveChat(String content, UUID conversationId, ChatType chatType) {
        try {
            Conversation conversation = conversationRepository.getReferenceById(conversationId);
            Chat chat = Chat.builder().content(content).conversation(conversation).type(chatType).build();
            chatRepository.save(chat);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveConversation(UUID conversationId, String summary, String recentChats, String name, boolean isProperName) {
        try {
            conversationRepository.updateConversation(conversationId, summary, recentChats, name, isProperName);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

}
