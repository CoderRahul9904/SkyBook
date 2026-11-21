package com.skybook.ai.mcp;

import com.skybook.ai.mcp.dto.McpRpcRequest;
import com.skybook.ai.mcp.dto.McpRpcResponse;
import com.skybook.flight.FlightService;
import com.skybook.flight.dto.FlightResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class McpControllerTest {

    @Test
    void toolsListReturnsAvailableTools() {
        FlightService flightService = mock(FlightService.class);
        McpController controller = new McpController(flightService);

        ResponseEntity<McpRpcResponse> response = controller.rpc(new McpRpcRequest("2.0", 1, "tools/list", Map.of()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().result()).isInstanceOf(Map.class);
    }

    @Test
    void toolsCallFlightDetailsInvokesFlightService() {
        FlightService flightService = mock(FlightService.class);
        McpController controller = new McpController(flightService);
        when(flightService.getDetails(42L)).thenReturn(null);

        McpRpcRequest request = new McpRpcRequest(
                "2.0",
                "abc",
                "tools/call",
                Map.of(
                        "name", "flight.details",
                        "arguments", Map.of("flightId", 42)
                )
        );

        ResponseEntity<McpRpcResponse> response = controller.rpc(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isNull();
    }

    @Test
    void toolsCallFlightSearchReturnsData() {
        FlightService flightService = mock(FlightService.class);
        McpController controller = new McpController(flightService);
        when(flightService.search("DEL", "BLR", java.time.LocalDate.parse("2026-12-01"), 1, com.skybook.flight.CabinClass.ECONOMY))
                .thenReturn(List.<FlightResponse>of());

        McpRpcRequest request = new McpRpcRequest(
                "2.0",
                2,
                "tools/call",
                Map.of(
                        "name", "flight.search",
                        "arguments", Map.of(
                                "origin", "DEL",
                                "destination", "BLR",
                                "travelDate", "2026-12-01",
                                "passengerCount", 1,
                                "cabinClass", "ECONOMY"
                        )
                )
        );

        ResponseEntity<McpRpcResponse> response = controller.rpc(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isNull();
    }
}
