package se.devsecops.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import se.devsecops.backend.model.HealthResponse;

class HealthControllerTest {

    private final HealthController healthController = new HealthController();

    @Test
    void healthReturnsOkStatus() {
        ResponseEntity<HealthResponse> response = healthController.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ok", response.getBody().getStatus());
        assertEquals("Backend is running", response.getBody().getMessage());
    }
}
