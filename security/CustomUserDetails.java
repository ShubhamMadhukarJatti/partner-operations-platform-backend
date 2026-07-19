package com.sharkdom.security;

import lombok.Getter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.stream.Stream;
@Getter
public class CustomUserDetails extends User {
    private final Long organizationId;

    public CustomUserDetails(String username, String password, Long organizationId, String... authorities) {
        super(username, password, Stream.of(authorities).map(a -> (GrantedAuthority) () -> a).toList());
        this.organizationId = organizationId;
    }
} 