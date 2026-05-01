package com.e_commerce.e_commerce.mapper;

import com.e_commerce.e_commerce.dto.request.PermissionRequest;
import com.e_commerce.e_commerce.dto.response.PermissionResponse;
import com.e_commerce.e_commerce.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest permissionRequest);

    PermissionResponse toPermissionResponse(Permission permission);
}
