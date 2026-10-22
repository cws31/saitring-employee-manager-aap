package dev.sonu.saitring_employee_manager.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ObservabilityAndHealthTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Actuator /actuator/health endpoint is accessible and returns health status")
    void testActuatorHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @DisplayName("Actuator /actuator/info endpoint is accessible")
    void testActuatorInfoEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TraceIdFilter generates X-Trace-Id header on outbound responses")
    void testTraceIdGeneratedOnResponse() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Trace-Id"))
                .andExpect(header().string("X-Trace-Id", not(emptyOrNullString())));
    }

    @Test
    @DisplayName("TraceIdFilter preserves incoming X-Trace-Id or X-Request-Id from caller")
    void testTraceIdPreservedFromClient() throws Exception {
        String clientTraceId = "client-trace-abc-123";

        mockMvc.perform(get("/actuator/health")
                        .header("X-Trace-Id", clientTraceId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", clientTraceId));
    }

    @Test
    @DisplayName("Validation and error responses embed traceId in ErrorResponse body")
    void testErrorResponseContainsTraceId() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"invalid-email\", \"password\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Trace-Id"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.traceId", not(emptyOrNullString())));
    }
}
