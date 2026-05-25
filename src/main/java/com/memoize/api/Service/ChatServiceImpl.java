package com.memoize.api.Service;

import com.memoize.api.Config.Common;
import com.memoize.api.Dto.ChatDto;
import com.memoize.api.Entity.Chat;
import com.memoize.api.Entity.Conversation;
import com.memoize.api.Enum.ChatType;
import com.memoize.api.Repository.ChatRepository;
import com.memoize.api.Repository.ConversationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
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
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        String queryPrompt = buildQueryWithContext(query, conversation);
        this.saveChat(query, conversation, ChatType.QUESTION);
        StringBuilder answerBuilder = new StringBuilder();
        return chatClient.prompt(queryPrompt).stream().content()
                .doOnNext(answerBuilder::append)
                .doOnComplete(() -> CompletableFuture.runAsync(() -> {
                    String fullAnswer = answerBuilder.toString();
                    this.saveChat(fullAnswer, conversation, ChatType.ANSWER);
                    this.manageConversation(conversation, query, fullAnswer);
                }));
    }

    // helpers
    private boolean isValidConversation(UUID conversationId, UUID userId) {
        return conversationRepository.existsByIdAndUserId(conversationId, userId);
    }

    private void saveChat(String content, Conversation conversation, ChatType chatType) {
        try {
            Chat chat = Chat.builder().conversation(conversation).content(content).type(chatType).build();
            chatRepository.saveAndFlush(chat);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private void manageConversation(Conversation conversation, String currentQuery, String currentResponse) {
        try {
            String recentChats = updateRecentChats(conversation, currentQuery, currentResponse);
            String summary = conversation.getSummary();
            CompletableFuture<String> updatedSummaryFuture = CompletableFuture.supplyAsync(() -> generateUpdatedSummary(summary, recentChats));
            CompletableFuture<String> generatedNameFuture = null;
            if (!conversation.isProperName()) {
                generatedNameFuture = CompletableFuture.supplyAsync(() -> generateNameForConversation(currentQuery, currentResponse));
            }
            conversation.setSummary(updatedSummaryFuture.join());
            if (generatedNameFuture != null) {
                conversation.setName(generatedNameFuture.join());
                conversation.setProperName(true);
            }
            conversation.setRecentChats(recentChats);
            conversation.setNew(false);
            conversationRepository.saveAndFlush(conversation);
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

    private String safe(String input) {
        return (input == null || input.isBlank()) ? "None" : input;
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
