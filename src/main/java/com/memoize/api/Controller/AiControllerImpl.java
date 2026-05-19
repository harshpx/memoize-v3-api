package com.memoize.api.Controller;

import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.AiChatDto;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiControllerImpl implements AiController{
    private final AiChatService aiChatService;

    @Override
    @GetMapping("/chats")
    public ResponseEntity<CommonResponse<Page<AiChatDto>>> getLlmChatsOfUser(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PageableDefault(
                    size = 50,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(CommonResponse.success(aiChatService.getLlmChatsOfUser(principal.userId(), pageable)));
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
