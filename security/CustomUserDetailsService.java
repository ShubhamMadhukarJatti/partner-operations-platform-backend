package com.sharkdom.security;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final OrganizationUserMappingRepository organizationUserMappingRepository;
    private final UserRepository userRepository;

    public CustomUserDetailsService(OrganizationUserMappingRepository organizationUserMappingRepository, UserRepository userRepository) {
        this.organizationUserMappingRepository = organizationUserMappingRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        var optionalUser = userRepository.findByEmail(username);
        if (optionalUser.isPresent()) {
            var user = optionalUser.get();
            var orgMapping = organizationUserMappingRepository.findAllByUserId(user.getUserId()).stream().findFirst();
            return orgMapping.map(organizationUserMapping -> 
                new CustomUserDetails(user.getEmail(), "", organizationUserMapping.getOrganizationId(), "ROLE_" + organizationUserMapping.getRole().name()))
                .orElseGet(() -> new CustomUserDetails(user.getEmail(), "", null, "ROLE_" + OrgUserRole.ADMIN.name()));
        }
        throw new ServiceException(ErrorMessages.SH03);
    }
}
