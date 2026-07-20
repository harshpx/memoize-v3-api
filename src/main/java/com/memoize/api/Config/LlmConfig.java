package com.memoize.api.Config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmConfig {
    @Bean(name = "vector-store-gemini-3.5-flash")
    public ChatClient geminiChatClient1(
        GoogleGenAiChatModel chatModel,
        @Qualifier("chat-memory-vector-store") VectorStore vectorStore
    ) {
        VectorStoreChatMemoryAdvisor memoryAdvisor = VectorStoreChatMemoryAdvisor.builder(vectorStore).build();
        return ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().model("gemini-3.5-flash").temperature(0.3))
                .defaultAdvisors(memoryAdvisor)
                .build();
    }

    @Bean(name = "vector-store-gemini-3.1-flash-lite")
    public ChatClient geminiChatClient2(
        GoogleGenAiChatModel chatModel,
        @Qualifier("chat-memory-vector-store") VectorStore vectorStore
    ) {
        VectorStoreChatMemoryAdvisor memoryAdvisor = VectorStoreChatMemoryAdvisor.builder(vectorStore).build();
        return ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().model("gemini-3.1-flash-lite").temperature(0.3))
                .defaultAdvisors(memoryAdvisor)
                .build();
    }

    @Bean(name = "simple-gemini-3.1-flash-lite")
    public ChatClient geminiChatClient3(GoogleGenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().model("gemini-3.1-flash-lite").temperature(0.3))
                .build();
    }
}
