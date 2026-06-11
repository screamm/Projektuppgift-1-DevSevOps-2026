package se.devsecops.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import se.devsecops.backend.model.DashboardSummaryResponse;
import se.devsecops.backend.service.UserService;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    void getSummaryReturnsOkWhenUserExists() {
        DashboardSummaryResponse expectedResponse = new DashboardSummaryResponse(
            "Test User",
            "test@example.com",
            "active",
            1,
            3,
            0,
            3
        );
        when(userService.getDashboardSummary("test@example.com")).thenReturn(expectedResponse);

        ResponseEntity<DashboardSummaryResponse> response =
            dashboardController.getSummary("test@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(userService).getDashboardSummary("test@example.com");
    }

    @Test
    void getSummaryReturnsNotFoundWhenUserIsMissing() {
        DashboardSummaryResponse expectedResponse =
            new DashboardSummaryResponse("User not found");
        when(userService.getDashboardSummary("missing@example.com")).thenReturn(expectedResponse);

        ResponseEntity<DashboardSummaryResponse> response =
            dashboardController.getSummary("missing@example.com");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }
}
