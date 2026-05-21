package com.memoize.api.Service;

import com.memoize.api.Dto.ChatDto;
import com.memoize.api.Entity.Chat;
import com.memoize.api.Entity.Conversation;
import com.memoize.api.Enum.ChatType;
import com.memoize.api.Repository.ChatRepository;
import com.memoize.api.Repository.ConversationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final ConversationRepository conversationRepository;
    private final ChatClient chatClient;

    @Autowired
    public ChatServiceImpl(ChatRepository chatRepository, ConversationRepository conversationRepository, ChatClient.Builder chatClientBuilder) {
        this.chatRepository = chatRepository;
        this.conversationRepository = conversationRepository;
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public List<ChatDto> getChatsOfConversation(UUID conversationId, UUID userId) {
        if (!isValidConversation(conversationId, userId)) {
            throw new IllegalArgumentException("Invalid/Incorrect conversationId user combination");
        }
        return chatRepository.fetchChatsByConversationId(conversationId);
    }

    @Override
    public Flux<String> queryLlmStream(String query, UUID conversationId, UUID userId) {
        if (!isValidConversation(conversationId, userId)) {
            throw new IllegalArgumentException("Invalid/Incorrect conversationId user combination");
        }
        this.saveChat(query, conversationId, ChatType.QUESTION);
        StringBuilder answerBuilder = new StringBuilder();
        return chatClient.prompt(query).stream().content()
                .doOnNext(answerBuilder::append)
                .doOnComplete(() -> {
                    String fullAnswer = answerBuilder.toString();
                    this.saveChat(fullAnswer, conversationId, ChatType.ANSWER);
                });
    }

    // helpers
    private boolean isValidConversation(UUID conversationId, UUID userId) {
        return conversationRepository.existsByIdAndUserId(conversationId, userId);
    }
    public void saveChat(String content, UUID conversationId, ChatType chatType) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new EntityNotFoundException("conversation not found"));
            Chat chat = Chat.builder().conversation(conversation).content(content).type(chatType).build();
            chatRepository.saveAndFlush(chat);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
