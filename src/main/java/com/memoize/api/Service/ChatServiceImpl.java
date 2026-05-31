package com.memoize.api.Service;

import com.memoize.api.Config.Common;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final ConversationRepository conversationRepository;
    private final ChatPersistanceService chatPersistanceService;
    private final ChatClient chatClient;
    private final Executor llmTaskExecutor;

    @Autowired
    public ChatServiceImpl(ChatRepository chatRepository,
                           ConversationRepository conversationRepository,
                           ChatPersistanceService chatPersistanceService,
                           @Qualifier("gemini-3.1-flash-lite-chat-client") ChatClient chatClient,
                           @Qualifier("llmTaskExecutor") Executor llmTaskExecutor) {
        this.chatRepository = chatRepository;
        this.conversationRepository = conversationRepository;
        this.chatClient = chatClient;
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
            String queryPrompt = buildQueryWithContext(query, conversation);
            StringBuffer answerBuffer = new StringBuffer();
            return chatClient.prompt(queryPrompt)
                .stream()
                .content()
                .timeout(Duration.ofSeconds(10))
                .doOnNext(answerBuffer::append)
                .doOnComplete(() -> {
                    CompletableFuture.runAsync(() -> {
                        String fullAnswer = answerBuffer.toString();
                        chatPersistanceService.saveChat(fullAnswer, conversation.getId(), ChatType.ANSWER);
                        this.onLLMSuccessHandler(conversation, query, fullAnswer);
                    }, llmTaskExecutor);
                })
                .onErrorResume(error -> {
                    String fullAnswer = "Error generating response";
                    CompletableFuture.runAsync(() -> {
                        chatPersistanceService.saveChat(fullAnswer, conversation.getId(), ChatType.ANSWER);
                        this.onLLMErrorHandler(conversation, query, fullAnswer);
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
            String recentChats = updateRecentChats(conversation, currentQuery, currentResponse);
            String name = conversation.getName();
            boolean isProperName = conversation.isProperName();
            if (!isProperName) {
                name = generateNameForConversation(currentQuery, currentResponse);
                isProperName = true;
            }
            String summary = generateUpdatedSummary(conversation.getSummary(), recentChats);
            chatPersistanceService.saveConversation(conversation.getId(), summary, recentChats, name, isProperName);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private void onLLMErrorHandler(Conversation conversation, String currentQuery, String currentResponse) {
        try {
            String summary = conversation.getSummary();
            String recentChats = updateRecentChats(conversation, currentQuery, currentResponse);
            boolean isProperName = conversation.isProperName();
            String name = isProperName ? conversation.getName() : "Error in response generation";
            chatPersistanceService.saveConversation(conversation.getId(), summary, recentChats, name, isProperName);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private String updateRecentChats(Conversation conversation, String currentQuery, String currentResponse) {
        String recentChatString = conversation.getRecentChats() != null ? conversation.getRecentChats() : "";
        Queue<String> recentChatsQueue = Arrays.stream(recentChatString.split(Common.DELIMITER))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(ArrayDeque::new));
        int size = recentChatsQueue.size();
        if (size >= 6) {
            for (int i = 0; i < 2; i++) {
                recentChatsQueue.poll();
            }
        }
        recentChatsQueue.offer("User: " + currentQuery);
        recentChatsQueue.offer("Assistant: " + currentResponse);
        return String.join(Common.DELIMITER, recentChatsQueue);
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

        return chatClient.prompt(context).call().content();
    }

    private String buildQueryWithContext(String query, Conversation conversation) {
        String rollingSummary = conversation.getSummary();
        String recentChats = conversation.getRecentChats() != null ? conversation.getRecentChats() : "";
        String chatsFormatted = Arrays.stream(recentChats.split(Common.DELIMITER))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));

        return """
            Conversation summary (long-term context):
            %s

            Recent conversations (last interactions):
            %s

            Instructions:
            - Use both the summary and recent conversation to understand context
            - Give a clear, accurate, and helpful answer
            - Be concise but complete
            - Do NOT repeat previous answers unless necessary
            - If context is insufficient, rely on the current question

            Current User Question:
            %s

            Answer:
        """.formatted(safe(rollingSummary), safe(chatsFormatted), safe(query));
    }

    private String generateUpdatedSummary(String previousSummary, String recentChats) {
        String[] temp = recentChats.split(Common.DELIMITER);
        recentChats = String.join("\n", temp);
        String prompt = """
            You are maintaining a conversation summary using:

            Previous Summary:
            %s

            Recent Conversations:
            %s
  
            Instructions:
            - Update the summary by incorporating new important information
            - Keep it concise and structured
            - Preserve key facts, decisions, preferences, and topics
            - Remove redundant or less important details
            - Keep the summary under 150 words
            - Do NOT repeat the entire conversation
            - Output ONLY the updated summary

            Updated Summary:
        """.formatted(safe(previousSummary), safe(recentChats));
        return chatClient.prompt(prompt).call().content();
    }
}
