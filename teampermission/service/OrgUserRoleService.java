package com.sharkdom.teampermission.service;

import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.teampermission.models.RoleResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrgUserRoleService {

    public List<RoleResponse> getAllRoles() {
        return Arrays.stream(OrgUserRole.values())
                .map(role -> new RoleResponse(
                        role.name(),
                        formatRoleName(role.name())
                ))
                .collect(Collectors.toList());
    }

    private String formatRoleName(String enumName) {
        String[] words = enumName.toLowerCase().split("_");

        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            formatted.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        return formatted.toString().trim();
    }
}

