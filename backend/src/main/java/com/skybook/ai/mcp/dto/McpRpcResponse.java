package com.skybook.ai.mcp.dto;

import java.util.Map;

public record McpRpcResponse(
        String jsonrpc,
        Object id,
        Object result,
        Map<String, Object> error
) {

    public static McpRpcResponse ok(Object id, Object result) {
        return new McpRpcResponse("2.0", id, result, null);
    }

    public static McpRpcResponse error(Object id, int code, String message) {
        return new McpRpcResponse("2.0", id, null, Map.of("code", code, "message", message));
    }
}
