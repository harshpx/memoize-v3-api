package com.memoize.api.Config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmConfig {
    @Bean(name = "gemini-3.5-flash-chat-client")
    public ChatClient geminiChatClient1(GoogleGenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().model("gemini-3.5-flash").temperature(0.3))
                .build();
    }

    @Bean(name = "gemini-3.1-flash-lite-chat-client")
    public ChatClient geminiChatClient2(GoogleGenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().model("gemini-3.1-flash-lite").temperature(0.3))
                .build();
    }
}
