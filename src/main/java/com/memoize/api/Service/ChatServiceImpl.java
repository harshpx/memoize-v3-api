package com.memoize.api.Service;

import com.memoize.api.Dto.ChatDto;
import com.memoize.api.Entity.Conversation;
import com.memoize.api.Enum.ChatType;
import com.memoize.api.Repository.ChatRepository;
import com.memoize.api.Repository.ConversationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final ConversationRepository conversationRepository;
    private final ChatPersistanceService chatPersistanceService;
    private final ChatClient vectorStoreChatClient;
    private final ChatClient simpleChatClient;
    private final Executor llmTaskExecutor;

    @Autowired
    public ChatServiceImpl(ChatRepository chatRepository,
                           ConversationRepository conversationRepository,
                           ChatPersistanceService chatPersistanceService,
                           @Qualifier("simple-gemini-3.1-flash-lite") ChatClient simpleChatClient,
                           @Qualifier("vector-store-gemini-3.1-flash-lite") ChatClient vectorStoreChatClient,
                           @Qualifier("llmTaskExecutor") Executor llmTaskExecutor) {
        this.chatRepository = chatRepository;
        this.conversationRepository = conversationRepository;
        this.simpleChatClient = simpleChatClient;
        this.vectorStoreChatClient = vectorStoreChatClient;
        this.chatPersistanceService = chatPersistanceService;
        this.llmTaskExecutor = llmTaskExecutor;
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
        return Mono.fromCallable(() -> {
            Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                    .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
            chatPersistanceService.saveChat(query, conversation.getId(), ChatType.QUESTION);
            return conversation;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(conversation -> {
            StringBuilder answerChunks = new StringBuilder();
            return vectorStoreChatClient.prompt().user(query)
                .advisors(advisor -> advisor
                        .param("chat_memory_conversation_id", conversationId)
                        .param("chat_memory_response_size", 5))
                .stream()
                .content()
                .timeout(Duration.ofSeconds(30))
                .doOnNext(answerChunks::append)
                .doOnComplete(() -> CompletableFuture.runAsync(() -> {
                    String fullResponse = answerChunks.toString();
                    this.onLLMSuccessHandler(conversation, query, fullResponse);
                }, llmTaskExecutor))
                .onErrorResume((error) -> {
                    String fullAnswer = "Error generating response";
                    CompletableFuture.runAsync(() -> {
                        chatPersistanceService.saveChat(fullAnswer, conversation.getId(), ChatType.ANSWER);
                    }, llmTaskExecutor);
                    return Flux.just(fullAnswer);
                });
        });
    }

    // helpers
    private boolean isValidConversation(UUID conversationId, UUID userId) {
        return conversationRepository.existsByIdAndUserId(conversationId, userId);
    }

    private String safe(String input) {
        return (input == null || input.isBlank()) ? "None" : input;
    }

    private void onLLMSuccessHandler(Conversation conversation, String currentQuery, String currentResponse) {
        try {
            chatPersistanceService.saveChat(currentResponse, conversation.getId(), ChatType.ANSWER);
            String name = conversation.getName();
            boolean isProperName = conversation.isProperName();
            if (!isProperName) {
                name = generateNameForConversation(currentQuery, currentResponse);
            }
            chatPersistanceService.setConversationName(conversation.getId(), name);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private String generateNameForConversation(String firstQuery, String firstResponse) {
        String context = """
            Create a concise conversation title.

            Rules:
            - 3 to 5 words only
            - Plain text only
            - No quotes, no punctuation
            - No extra text
    
            Example outputs:
            Java Stream Explanation
            React State Management
            SQL Join Basics
    
            Query: %s
            Response: %s
        """.formatted(firstQuery, firstResponse);

        return simpleChatClient.prompt(context).call().content();
    }
}
