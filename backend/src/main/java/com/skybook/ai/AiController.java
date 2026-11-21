package com.skybook.ai;

import com.skybook.ai.dto.AiChatRequest;
import com.skybook.ai.dto.AiChatResponse;
import com.skybook.ai.dto.AiConversationDetailResponse;
import com.skybook.ai.dto.AiConversationMessageResponse;
import com.skybook.ai.dto.AiConversationSummaryResponse;
import com.skybook.auth.User;
import com.skybook.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AeroMate AI")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AeroMateService aeroMateService;
    private final AiConversationService aiConversationService;

    public AiController(AeroMateService aeroMateService, AiConversationService aiConversationService) {
        this.aeroMateService = aeroMateService;
        this.aiConversationService = aiConversationService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a chat message to AeroMate")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AiChatRequest request
    ) {
        AiChatResponse response = aeroMateService.chat(user, request);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "AI response generated", response));
    }

    @GetMapping("/conversations")
    @Operation(summary = "List authenticated user's AI conversations")
    public ResponseEntity<ApiResponse<List<AiConversationSummaryResponse>>> listConversations(
            @AuthenticationPrincipal User user
    ) {
        List<AiConversationSummaryResponse> conversations = aiConversationService.listOwnedConversations(user).stream()
                .map(conversation -> new AiConversationSummaryResponse(
                        conversation.getConversationId(),
                        conversation.getCreatedAt(),
                        conversation.getUpdatedAt()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Conversations retrieved", conversations));
    }

    @GetMapping("/conversations/{conversationId}")
    @Operation(summary = "Get one owned conversation with message history")
    public ResponseEntity<ApiResponse<AiConversationDetailResponse>> getConversation(
            @AuthenticationPrincipal User user,
            @PathVariable String conversationId
    ) {
        AiConversation conversation = aiConversationService.getOwnedConversation(user, conversationId);
        List<Message> messages = aiConversationService.getOwnedConversationMessages(user, conversationId);
        List<AiConversationMessageResponse> mappedMessages = messages.stream()
                .map(message -> new AiConversationMessageResponse(
                        message.getMessageType().getValue(),
                        message.getText()
                ))
                .toList();

        AiConversationDetailResponse response = new AiConversationDetailResponse(
                conversation.getConversationId(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                mappedMessages
        );

        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Conversation retrieved", response));
    }
}
