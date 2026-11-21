package com.skybook.ai.dto;

public record AiChatResponse(
        String conversationId,
        String response,
        boolean ragApplied
) {
}
