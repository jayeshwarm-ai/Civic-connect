package com.civicconnect.identity.controller;

import com.civicconnect.identity.dto.request.CreateStaffRequest;
import com.civicconnect.identity.dto.request.UpdateMyProfileRequest;
import com.civicconnect.identity.dto.response.StaffResponse;
import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.enums.UserStatus;
import com.civicconnect.identity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock private UserService userService;

    @InjectMocks
    private UserController controller;

    private UsernamePasswordAuthenticationToken authWithUserId(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
        auth.setDetails(userId);
        return auth;
    }

    private StaffResponse sampleStaff;

    @BeforeEach
    void setUp() {
        sampleStaff = StaffResponse.builder()
                .userId(2L).name("Officer Bob").role(Role.SERVICE_OFFICER).build();
    }

    @Test
    @DisplayName("createStaff → 201")
    void createStaff_returns201() {
        CreateStaffRequest req = new CreateStaffRequest();
        when(userService.createStaff(eq(req), eq(1L))).thenReturn(sampleStaff);

        ResponseEntity<StaffResponse> resp = controller.createStaff(req, authWithUserId(1L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(userService).createStaff(eq(req), eq(1L));
    }

    @Test
    @DisplayName("getStaffByRole → 200")
    void getStaffByRole_returns200() {
        when(userService.getStaffByRole(Role.SERVICE_OFFICER)).thenReturn(List.of(sampleStaff));

        ResponseEntity<List<StaffResponse>> resp = controller.getStaffByRole(Role.SERVICE_OFFICER);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getStaffById → 200")
    void getStaffById_returns200() {
        when(userService.getStaffById(2L)).thenReturn(sampleStaff);

        ResponseEntity<StaffResponse> resp = controller.getStaffById(2L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getUserId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("updateStaffStatus → 200")
    void updateStaffStatus_returns200() {
        when(userService.updateStaffStatus(eq(2L), eq(UserStatus.SUSPENDED), eq(1L)))
                .thenReturn(sampleStaff);

        ResponseEntity<StaffResponse> resp = controller.updateStaffStatus(
                2L, UserStatus.SUSPENDED, authWithUserId(1L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userService).updateStaffStatus(eq(2L), eq(UserStatus.SUSPENDED), eq(1L));
    }

    @Test
    @DisplayName("getMyProfile → 200, delegates with extracted userId")
    void getMyProfile_returns200() {
        when(userService.getMyProfile(2L)).thenReturn(sampleStaff);

        ResponseEntity<StaffResponse> resp = controller.getMyProfile(authWithUserId(2L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userService).getMyProfile(2L);
    }

    @Test
    @DisplayName("updateMyProfile → 200")
    void updateMyProfile_returns200() {
        UpdateMyProfileRequest req = new UpdateMyProfileRequest();
        when(userService.updateMyProfile(eq(2L), eq(req))).thenReturn(sampleStaff);

        ResponseEntity<StaffResponse> resp = controller.updateMyProfile(req, authWithUserId(2L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userService).updateMyProfile(eq(2L), eq(req));
    }
}
