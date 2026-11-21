package com.skybook.ai.dto;

import java.time.OffsetDateTime;

public record AiConversationSummaryResponse(
        String conversationId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
