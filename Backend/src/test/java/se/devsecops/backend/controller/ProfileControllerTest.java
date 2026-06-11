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

import se.devsecops.backend.model.GetProfileResponse;
import se.devsecops.backend.model.UpdateProfileRequest;
import se.devsecops.backend.model.UpdateProfileResponse;
import se.devsecops.backend.service.UserService;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private ProfileController profileController;

    @Test
    void getProfileReturnsOkWhenUserExists() {
        GetProfileResponse expectedResponse =
            new GetProfileResponse("Test User", "test@example.com");
        when(userService.getProfile("test@example.com")).thenReturn(expectedResponse);

        ResponseEntity<GetProfileResponse> response =
            profileController.getProfile("test@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(userService).getProfile("test@example.com");
    }

    @Test
    void getProfileReturnsNotFoundWhenUserIsMissing() {
        GetProfileResponse expectedResponse = new GetProfileResponse("User not found");
        when(userService.getProfile("missing@example.com")).thenReturn(expectedResponse);

        ResponseEntity<GetProfileResponse> response =
            profileController.getProfile("missing@example.com");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }

    @Test
    void getProfileReturnsBadRequestForInvalidInput() {
        GetProfileResponse expectedResponse =
            new GetProfileResponse("Email address is invalid");
        when(userService.getProfile("invalid-email")).thenReturn(expectedResponse);

        ResponseEntity<GetProfileResponse> response =
            profileController.getProfile("invalid-email");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }

    @Test
    void updateProfileDelegatesToUserService() {
        UpdateProfileRequest request =
            new UpdateProfileRequest("test@example.com", "Updated User", "test@example.com");
        UpdateProfileResponse expectedResponse =
            new UpdateProfileResponse(true, "Profile updated", "test@example.com");
        when(userService.updateProfile(request)).thenReturn(expectedResponse);

        ResponseEntity<UpdateProfileResponse> response =
            profileController.updateProfile(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(userService).updateProfile(request);
    }

    @Test
    void updateProfileReturnsBadRequestForValidationErrors() {
        UpdateProfileRequest request =
            new UpdateProfileRequest("test@example.com", "", "test@example.com");
        UpdateProfileResponse expectedResponse =
            new UpdateProfileResponse(false, "Name cannot be empty");
        when(userService.updateProfile(request)).thenReturn(expectedResponse);

        ResponseEntity<UpdateProfileResponse> response =
            profileController.updateProfile(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }
}
