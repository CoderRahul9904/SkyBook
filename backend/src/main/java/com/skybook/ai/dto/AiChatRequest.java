package com.skybook.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
        @NotBlank
        @Size(max = 4000)
        String message,

        @Size(max = 100)
        String conversationId
) {
}
