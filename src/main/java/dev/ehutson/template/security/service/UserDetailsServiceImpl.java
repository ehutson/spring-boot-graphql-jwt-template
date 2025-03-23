package dev.ehutson.template.security.service;

import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserModel user = userRepository.findOneByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username:  " + username));

        log.debug("User found with username: {}", username);

        return UserDetailsImpl.build(user);
    }
}
