package com.memoize.api.Service;

import com.memoize.api.Dto.UserInfo;
import com.memoize.api.Entity.User;
import com.memoize.api.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserInfo getUserInfo(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return UserInfo.fromEntity(user);
    }
}
