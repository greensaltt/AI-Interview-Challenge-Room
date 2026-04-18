package com.offerdungeon.common;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(CommonApiContractTest.TestExceptionController.class)
class CommonApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldWrapSuccessfulResponsesWithUnifiedContract() throws Exception {
        String requestId = "step4-success-request";

        mockMvc.perform(get("/api/health").header("X-Request-Id", requestId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", requestId))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("backend"))
                .andExpect(jsonPath("$.data.taskStatusEndpoint").value("/api/tasks/{taskId}"));
    }

    @Test
    void shouldReturnUnifiedValidationErrorForInvalidParameters() throws Exception {
        String requestId = "step4-validation-request";

        mockMvc.perform(get("/api/tasks/a").header("X-Request-Id", requestId))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Request-Id", requestId))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("taskId"));
    }

    @Test
    void shouldReturnUnifiedInternalServerErrorForUnexpectedFailures() throws Exception {
        String requestId = "step4-exception-request";

        mockMvc.perform(get("/api/test/boom").header("X-Request-Id", requestId))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("X-Request-Id", requestId))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("The system encountered an unexpected error."))
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @RestController
    @RequestMapping("/api/test")
    static class TestExceptionController {

        @GetMapping("/boom")
        public String boom() {
            throw new IllegalStateException("Simulated unexpected failure for contract testing.");
        }
    }
}
