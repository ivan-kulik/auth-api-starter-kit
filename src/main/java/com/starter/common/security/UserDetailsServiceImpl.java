package com.starter.common.security;

import com.starter.feature.auth.service.UserIdentifierResolver;
import com.starter.feature.auth.service.UserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserIdentifierResolver userIdentifierResolver;
    private final UserLookupService userLookupService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        var identifier = this.userIdentifierResolver.resolve(login);
        var user = this.userLookupService.find(identifier);

        return new CustomUserDetails(user);
    }
}
