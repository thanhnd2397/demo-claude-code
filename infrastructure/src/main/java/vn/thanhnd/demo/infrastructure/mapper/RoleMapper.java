package vn.thanhnd.demo.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.thanhnd.demo.domain.model.Role;
import vn.thanhnd.demo.infrastructure.persistence.entity.PermissionEntity;
import vn.thanhnd.demo.infrastructure.persistence.entity.RoleEntity;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissionNames", expression = "java(mapPermissionNames(entity.getPermissions()))")
    Role toDomain(RoleEntity entity);

    default Set<String> mapPermissionNames(Set<PermissionEntity> permissions) {
        return permissions == null
                ? Set.of()
                : permissions.stream().map(PermissionEntity::getName).collect(Collectors.toSet());
    }
}
