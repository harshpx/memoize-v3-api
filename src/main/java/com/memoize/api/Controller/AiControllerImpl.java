package com.memoize.api.Controller;

import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.AiChatDto;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiControllerImpl implements AiController{
    private final AiChatService aiChatService;

    @Override
    @GetMapping("/chat/all")
    public ResponseEntity<CommonResponse<List<AiChatDto>>> getLlmChatsOfUser(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(CommonResponse.success(aiChatService.getLlmChatsOfUser(principal.userId())));
    }

    @Override
    @PostMapping("/chat")
    public ResponseEntity<CommonResponse<String>> llmChat(
            @RequestParam(name = "query") String query,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(CommonResponse.success(aiChatService.llmChat(query, principal.userId())));
    }

    @Override
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> llmChatStream(
            @RequestParam(name = "query") String query,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(aiChatService.llmChatStream(query, principal.userId()));
    }
}
