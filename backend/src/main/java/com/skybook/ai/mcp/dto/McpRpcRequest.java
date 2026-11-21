package com.skybook.ai.mcp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record McpRpcRequest(
        @NotBlank
        String jsonrpc,

        @NotNull
        Object id,

        @NotBlank
        String method,

        Map<String, Object> params
) {
}
