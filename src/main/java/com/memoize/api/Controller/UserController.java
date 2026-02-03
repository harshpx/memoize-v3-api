package com.memoize.api.Controller;

import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Dto.UserInfo;
import org.springframework.http.ResponseEntity;

public interface UserController {
    ResponseEntity<CommonResponse<UserInfo>> getUserInfo(AuthPrincipal authPrincipal);
}
