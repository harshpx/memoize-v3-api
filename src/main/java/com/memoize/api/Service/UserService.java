package com.memoize.api.Service;

import com.memoize.api.Dto.UserInfo;

import java.util.UUID;

public interface UserService {
    UserInfo getUserInfo(UUID userId);
}
