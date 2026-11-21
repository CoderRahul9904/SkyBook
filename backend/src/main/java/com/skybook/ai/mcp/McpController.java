package com.skybook.ai.mcp;

import com.skybook.ai.mcp.dto.McpRpcRequest;
import com.skybook.ai.mcp.dto.McpRpcResponse;
import com.skybook.common.exception.BusinessRuleViolationException;
import com.skybook.flight.CabinClass;
import com.skybook.flight.FlightService;
import com.skybook.flight.dto.FlightResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mcp")
public class McpController {

    private final FlightService flightService;

    public McpController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping("/rpc")
    public ResponseEntity<McpRpcResponse> rpc(@Valid @RequestBody McpRpcRequest request) {
        if (!"2.0".equals(request.jsonrpc())) {
            return ResponseEntity.badRequest().body(McpRpcResponse.error(request.id(), -32600, "Invalid jsonrpc version"));
        }

        try {
            Object result = switch (request.method()) {
                case "tools/list" -> handleToolsList();
                case "tools/call" -> handleToolsCall(request.params());
                default -> throw new BusinessRuleViolationException("Method not found");
            };
            return ResponseEntity.ok(McpRpcResponse.ok(request.id(), result));
        }
        catch (BusinessRuleViolationException ex) {
            return ResponseEntity.badRequest().body(McpRpcResponse.error(request.id(), -32602, ex.getMessage()));
        }
        catch (Exception ex) {
            return ResponseEntity.internalServerError().body(McpRpcResponse.error(request.id(), -32000, "MCP request failed"));
        }
    }

    private Map<String, Object> handleToolsList() {
        Map<String, Object> searchTool = new LinkedHashMap<>();
        searchTool.put("name", "flight.search");
        searchTool.put("description", "Search flights by route, date, passenger count, and cabin class");

        Map<String, Object> detailsTool = new LinkedHashMap<>();
        detailsTool.put("name", "flight.details");
        detailsTool.put("description", "Get flight details by flightId");

        return Map.of("tools", List.of(searchTool, detailsTool));
    }

    @SuppressWarnings("unchecked")
    private Object handleToolsCall(Map<String, Object> params) {
        if (params == null) {
            throw new BusinessRuleViolationException("params is required");
        }

        String toolName = asRequiredString(params.get("name"), "name");
        Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Map.of());

        return switch (toolName) {
            case "flight.search" -> callFlightSearch(arguments);
            case "flight.details" -> callFlightDetails(arguments);
            default -> throw new BusinessRuleViolationException("Unknown tool: " + toolName);
        };
    }

    private List<FlightResponse> callFlightSearch(Map<String, Object> arguments) {
        String origin = asRequiredString(arguments.get("origin"), "origin");
        String destination = asRequiredString(arguments.get("destination"), "destination");
        String travelDate = asRequiredString(arguments.get("travelDate"), "travelDate");
        int passengerCount = asRequiredInteger(arguments.get("passengerCount"), "passengerCount");
        String cabinClass = asRequiredString(arguments.get("cabinClass"), "cabinClass");

        LocalDate date = LocalDate.parse(travelDate);
        CabinClass parsedCabinClass = CabinClass.valueOf(cabinClass.trim().toUpperCase());

        return flightService.search(origin, destination, date, passengerCount, parsedCabinClass);
    }

    private FlightResponse callFlightDetails(Map<String, Object> arguments) {
        long flightId = asRequiredLong(arguments.get("flightId"), "flightId");
        return flightService.getDetails(flightId);
    }

    private String asRequiredString(Object value, String name) {
        if (!(value instanceof String text) || text.isBlank()) {
            throw new BusinessRuleViolationException(name + " is required");
        }
        return text;
    }

    private int asRequiredInteger(Object value, String name) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new BusinessRuleViolationException(name + " must be a number");
    }

    private long asRequiredLong(Object value, String name) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new BusinessRuleViolationException(name + " must be a number");
    }
}
