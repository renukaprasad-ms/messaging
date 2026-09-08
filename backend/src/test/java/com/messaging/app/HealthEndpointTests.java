package com.messaging.app;

import com.messaging.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HealthEndpointTests {
    @Test
    void healthReturnsOk() throws Exception {
        MockMvcBuilders.standaloneSetup(new AppApplication())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build().perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void missingResourcesReturnNotFound() {
        var response = new GlobalExceptionHandler().handleNoResourceFound(
                new NoResourceFoundException(HttpMethod.GET, "favicon.ico", "/favicon.ico"));
        assertEquals(404, response.getStatusCode().value());
    }
}
