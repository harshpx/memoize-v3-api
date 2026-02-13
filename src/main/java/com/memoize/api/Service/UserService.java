package com.memoize.api.Service;

import com.memoize.api.Dto.UserDto;

import java.util.UUID;

public interface UserService {
    UserDto getUserInfo(UUID userId);
}
