package com.memoize.api.Config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmConfig {
    /*
    * Type: gemini-3.1-flash-lite
    * Chat memory: No
    * Documentation knowledge: No
    * */
    @Bean(name = "simple-gemini-3.1-flash-lite")
    public ChatClient simpleGemini31FlashLiteChatClient(GoogleGenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().model("gemini-3.1-flash-lite").temperature(0.3))
                .build();
    }

    /*
     * Model: gemini-3.1-flash-lite
     * Chat memory: Yes
     * Documentation knowledge: No
     * */
    @Bean(name = "memory-gemini-3.1-flash-lite")
    public ChatClient memoryGemini31FlashLiteChatClient(
            GoogleGenAiChatModel chatModel,
            @Qualifier("chat-memory-vector-store") VectorStore chatMemoryVectorStore
    ) {
        var memoryAdvisor = VectorStoreChatMemoryAdvisor.builder(chatMemoryVectorStore).defaultTopK(5).build();
        return ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().model("gemini-3.1-flash-lite").temperature(0.3))
                .defaultAdvisors(memoryAdvisor)
                .build();
    }

    /*
     * Model: gemini-3.1-flash-lite
     * Chat memory: Yes
     * Documentation knowledge: Yes
     * */
    @Bean(name = "memory-rag-gemini-3.1-flash-lite")
    public ChatClient geminiChatClient3(
            GoogleGenAiChatModel chatModel,
            @Qualifier("chat-memory-vector-store") VectorStore chatMemoryVectorStore,
            @Qualifier("knowledge-vector-store") VectorStore knowledgeVectorStore
    ) {
        var memoryAdvisor = VectorStoreChatMemoryAdvisor.builder(chatMemoryVectorStore).defaultTopK(5).build();
        var knowledgeAdvisor = QuestionAnswerAdvisor.builder(knowledgeVectorStore)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.55).build())
                .build();
        return ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().model("gemini-3.1-flash-lite").temperature(0.3))
                .defaultAdvisors(memoryAdvisor, knowledgeAdvisor)
                .build();
    }

    /*
     * Model: gemini-3.5-flash
     * Chat memory: Yes
     * Documentation knowledge: No
     * */
    @Bean(name = "memory-gemini-3.5-flash")
    public ChatClient memoryGemini35FlashChatClient(
            GoogleGenAiChatModel chatModel,
            @Qualifier("chat-memory-vector-store") VectorStore chatMemoryVectorStore
    ) {
        var memoryAdvisor = VectorStoreChatMemoryAdvisor.builder(chatMemoryVectorStore).defaultTopK(5).build();
        return ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().model("gemini-3.5-flash").temperature(0.3))
                .defaultAdvisors(memoryAdvisor)
                .build();
    }

    /*
     * Model: gemini-3.5-flash
     * Chat memory: Yes
     * Documentation knowledge: Yes
     * */
    @Bean(name = "memory-rag-gemini-3.5-flash")
    public ChatClient memoryRagGemini35FlashChatClient(
        GoogleGenAiChatModel chatModel,
        @Qualifier("chat-memory-vector-store") VectorStore chatMemoryVectorStore,
        @Qualifier("knowledge-vector-store") VectorStore knowledgeVectorStore
    ) {
        var memoryAdvisor = VectorStoreChatMemoryAdvisor.builder(chatMemoryVectorStore).defaultTopK(5).build();
        var knowledgeAdvisor = QuestionAnswerAdvisor.builder(knowledgeVectorStore)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.55).build())
                .build();
        return ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().model("gemini-3.5-flash").temperature(0.3))
                .defaultAdvisors(memoryAdvisor, knowledgeAdvisor)
                .build();
    }
}
