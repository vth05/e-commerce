package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.RoleRequest;
import com.e_commerce.e_commerce.dto.response.RoleResponse;
import com.e_commerce.e_commerce.entity.Permission;
import com.e_commerce.e_commerce.entity.Role;
import com.e_commerce.e_commerce.mapper.RoleMapper;
import com.e_commerce.e_commerce.repository.PermissionRepository;
import com.e_commerce.e_commerce.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    public RoleResponse createRole(RoleRequest roleRequest) {
        Role role = roleMapper.toRole(roleRequest);
        List<Permission> permissions = permissionRepository.findAllById(roleRequest.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    public List<RoleResponse> getRoles() {
        return roleRepository.findAll().stream().map((role) -> roleMapper.toRoleResponse(role)).toList();
    }

    public void deleteRole(String role) {
        roleRepository.deleteById(role);
    }
}
