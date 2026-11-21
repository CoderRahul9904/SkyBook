package com.skybook.ai.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record AiConversationDetailResponse(
        String conversationId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<AiConversationMessageResponse> messages
) {
}
