package com.memoize.api.Service;

import com.memoize.api.Dto.AiChatDto;
import com.memoize.api.Entity.AiChat;
import com.memoize.api.Entity.User;
import com.memoize.api.Enum.ChatType;
import com.memoize.api.Repository.AiChatRepository;
import com.memoize.api.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class AiChatServiceImpl implements AiChatService {

    private final ChatClient chatClient;
    private final UserRepository userRepository;
    private final AiChatRepository aiChatRepository;
    private final AiChatServiceImpl self;

    @Autowired
    public AiChatServiceImpl(
            ChatClient.Builder chatClientBuilder,
            AiChatRepository aiChatRepository,
            UserRepository userRepository,
            @Lazy AiChatServiceImpl self
    ) {
        this.chatClient = chatClientBuilder.build();
        this.userRepository = userRepository;
        this.aiChatRepository = aiChatRepository;
        this.self = self;
    }

    @Override
    public Page<AiChatDto> getLlmChatsOfUser(UUID userId, Pageable pageable) {
        return aiChatRepository.fetchChatsOfUser(userId, pageable);
    }

    @Override
    public String llmChat(String query, UUID userId) {
        this.saveAiChat(query, userId, ChatType.QUESTION);
        String fullAnswer = chatClient.prompt(query).call().content();
        this.saveAiChat(fullAnswer, userId, ChatType.ANSWER);
        return fullAnswer;
    }

    @Override
    public Flux<String> llmChatStream(String query, UUID userId) {
        self.saveAiChat(query, userId, ChatType.QUESTION);
        StringBuilder answerBuilder = new StringBuilder();
        return chatClient.prompt(query).stream().content()
                .doOnNext(answerBuilder::append)
                .doOnComplete(() -> {
                    String fullAnswer = answerBuilder.toString();
                    self.saveAiChat(fullAnswer, userId, ChatType.ANSWER);
                });
    }

    // helper
    public void saveAiChat(String content, UUID userId, ChatType chatType) {
        try {
            User currentUser = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
            AiChat chat = AiChat.builder().user(currentUser).type(chatType).content(content).build();
            aiChatRepository.saveAndFlush(chat);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
