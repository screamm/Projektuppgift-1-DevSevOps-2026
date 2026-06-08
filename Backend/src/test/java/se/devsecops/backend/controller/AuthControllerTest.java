package se.devsecops.backend.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.devsecops.backend.model.LoginRequest;
import se.devsecops.backend.model.LoginResponse;
import se.devsecops.backend.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginDelegatesToUserService() {
        LoginRequest request = new LoginRequest("test@example.com", "secret");
        LoginResponse expectedResponse = new LoginResponse(true, "Login successful");
        when(userService.login(request)).thenReturn(expectedResponse);

        LoginResponse response = authController.login(request);

        assertSame(expectedResponse, response);
        verify(userService).login(request);
    }
}
