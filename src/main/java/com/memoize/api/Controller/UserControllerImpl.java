package com.memoize.api.Controller;

import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Dto.UserDto;
import com.memoize.api.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<CommonResponse<UserDto>> getUserInfo(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        UUID userId = authPrincipal.userId();
        var response = CommonResponse.success(userService.getUserInfo(userId));
        return ResponseEntity.ok(response);
    }
}
