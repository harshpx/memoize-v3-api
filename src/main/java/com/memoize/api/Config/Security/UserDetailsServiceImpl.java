package com.memoize.api.Config.Security;

import com.memoize.api.Entity.User;
import com.memoize.api.Repository.UserRepository;
import lombok.*;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String identifier) throws UsernameNotFoundException {
        return userRepository
                .findByIdentifier(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with given credentials"));
    }
}
