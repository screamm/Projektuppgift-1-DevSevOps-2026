package se.devsecops.backend.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;
import se.devsecops.backend.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void createUserDelegatesToUserService() {
        CreateUserRequest request =
            new CreateUserRequest("Test User", "test@example.com", "secret");
        CreateUserResponse expectedResponse =
            new CreateUserResponse(false, "User created");
        when(userService.createUser(request)).thenReturn(expectedResponse);

        CreateUserResponse response = userController.createUser(request);

        assertSame(expectedResponse, response);
        verify(userService).createUser(request);
    }
}
