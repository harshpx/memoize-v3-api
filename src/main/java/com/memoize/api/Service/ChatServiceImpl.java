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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
        String queryPrompt = buildQueryWithContext(query, conversationId);
        this.saveChat(query, conversationId, ChatType.QUESTION);
        StringBuilder answerBuilder = new StringBuilder();
        return chatClient.prompt(queryPrompt).stream().content()
                .doOnNext(answerBuilder::append)
                .doOnComplete(() -> CompletableFuture.runAsync(() -> {
                    String fullAnswer = answerBuilder.toString();
                    this.saveChat(fullAnswer, conversationId, ChatType.ANSWER);
                    this.manageConversation(conversationId, query, fullAnswer);
                }));
    }

    // helpers
    private boolean isValidConversation(UUID conversationId, UUID userId) {
        return conversationRepository.existsByIdAndUserId(conversationId, userId);
    }

    private void saveChat(String content, UUID conversationId, ChatType chatType) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new EntityNotFoundException("Invalid/Incorrect conversationId user combination"));
            Chat chat = Chat.builder().conversation(conversation).content(content).type(chatType).build();
            chatRepository.saveAndFlush(chat);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void manageConversation(UUID conversationId, String currentQuery, String currentResponse) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() ->  new EntityNotFoundException("conversation not found"));
            String summary = conversation.getSummary();
            List<ChatDto> recentChats = chatRepository.fetchRecentChatsByConversationId(conversationId, PageRequest.of(0, 6)).reversed();
            String updatedSummary = generateUpdatedSummary(summary, recentChats);
            conversation.setSummary(updatedSummary);
            if (!conversation.isProperName()) {
                String generatedName = generateNameForConversation(currentQuery, currentResponse);
                conversation.setName(generatedName);
                conversation.setProperName(true);
            }
            conversation.setNew(false);
            conversationRepository.saveAndFlush(conversation);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
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

        return chatClient.prompt(context).call().content();
    }

    private String safe(String input) {
        return (input == null || input.isBlank()) ? "None" : input;
    }

    private String buildQueryWithContext(String query, UUID conversationId) {
        String rollingSummary = conversationRepository.findSummaryById(conversationId).orElse("");
        List<ChatDto> recentChats = chatRepository.fetchRecentChatsByConversationId(conversationId, PageRequest.of(0, 6)).reversed();
        String chatsFormatted = recentChats.stream()
                .map(chat -> (chat.type() == ChatType.QUESTION ? "User: " : "Assistant: ") + chat.content())
                .collect(Collectors.joining("\n"));
        return """
            You are a helpful AI assistant.

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

    private String generateUpdatedSummary(String previousSummary, List<ChatDto> recentChats) {
        String chatsFormatted = recentChats.stream()
                .map(chat -> (chat.type() == ChatType.QUESTION ? "User: " : "Assistant: " + chat.content()))
                .collect(Collectors.joining("\n"));
        String prompt = """
            You are a helpful AI assistant.
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
        """.formatted(safe(previousSummary), safe(chatsFormatted));
        return chatClient.prompt(prompt).call().content();
    }
}
