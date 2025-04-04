package dev.ehutson.template.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.ehutson.template.domain.UserModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private String id;
    private String username;
    private String email;
    private boolean enabled;
    private Collection<? extends GrantedAuthority> authorities;
    @JsonIgnore
    private String password;
    private String langKey;
    private String timezone;


    public static UserDetailsImpl build(UserModel user) {
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getName()))
                .toList();

        return UserDetailsImpl.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .enabled(user.isActivated())
                .langKey(user.getLangKey())
                .timezone(user.getTimezone())
                .build();
    }
}
