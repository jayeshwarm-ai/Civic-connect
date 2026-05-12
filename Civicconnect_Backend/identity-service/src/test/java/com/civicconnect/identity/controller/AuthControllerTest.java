package com.civicconnect.identity.controller;

import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.dto.request.CreateStaffRequest;
import com.civicconnect.identity.dto.request.LoginRequest;
import com.civicconnect.identity.dto.request.ResetPasswordRequest;
import com.civicconnect.identity.dto.response.LoginResponse;
import com.civicconnect.identity.dto.response.StaffResponse;
import com.civicconnect.identity.service.AuthService;
import com.civicconnect.identity.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock private AuthService authService;
    @Mock private UserService userService;

    @InjectMocks
    private AuthController controller;

    @Test
    @DisplayName("login → 200 with JWT response")
    void login_returns200() {
        LoginRequest req = new LoginRequest();
        LoginResponse loginResp = LoginResponse.builder()
                .token("jwt-token").userId(1L).role(Role.CITIZEN).build();
        when(authService.login(eq(req))).thenReturn(loginResp);

        ResponseEntity<LoginResponse> resp = controller.login(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getToken()).isEqualTo("jwt-token");
        verify(authService).login(eq(req));
    }

    @Test
    @DisplayName("resetPassword → 200 with success message")
    void resetPassword_returns200() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        doNothing().when(authService).resetPassword(eq(req));

        ResponseEntity<Map<String, String>> resp = controller.resetPassword(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsKey("message");
        assertThat(resp.getBody().get("message")).contains("Password reset successful");
        verify(authService).resetPassword(eq(req));
    }

    @Test
    @DisplayName("registerStaff → 201")
    void registerStaff_returns201() {
        CreateStaffRequest req = new CreateStaffRequest();
        StaffResponse staffResp = StaffResponse.builder().userId(1L).name("John").build();
        when(userService.registerStaffPublic(eq(req))).thenReturn(staffResp);

        ResponseEntity<StaffResponse> resp = controller.registerStaff(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getName()).isEqualTo("John");
        verify(userService).registerStaffPublic(eq(req));
    }
}
