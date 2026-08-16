package com.mel.expensetracker.authserver.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [FEATURE A3] JPA-backed UserDetailsService. Paired in
 * {@link com.mel.expensetracker.authserver.config.FormLoginSecurityConfig} with a
 * small in-memory UserDetailsService for comparison.
 *
 * <p>Implementing {@link UserDetailsPasswordService} lets DaoAuthenticationProvider
 * silently rewrite an outdated password encoding after a successful login.
 * [FEATURE A4]
 */
@Service
public class JpaUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user '%s'".formatted(username)));
        return AppUserPrincipal.from(user);
    }

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails userDetails, String newPassword) {
        User user = userRepository
                .findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(userDetails.getUsername()));
        user.setPasswordHash(newPassword);
        return AppUserPrincipal.from(user);
    }
}
