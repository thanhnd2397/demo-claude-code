package vn.thanhnd.demo.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.thanhnd.demo.domain.model.Administrator;
import vn.thanhnd.demo.infrastructure.persistence.entity.AdministratorEntity;
import vn.thanhnd.demo.infrastructure.persistence.entity.PermissionEntity;
import vn.thanhnd.demo.infrastructure.persistence.entity.RoleEntity;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps scalar fields between {@link Administrator} and {@link AdministratorEntity}. The
 * {@code roles}/{@code roleNames} relationship is resolved separately by the repository adapter,
 * since it requires a {@code RoleJpaRepository} lookup that a pure mapper cannot perform.
 */
@Mapper(componentModel = "spring")
public interface AdministratorMapper {

    @Mapping(target = "roleNames", expression = "java(mapRoleNames(entity.getRoles()))")
    @Mapping(target = "permissionNames", expression = "java(mapPermissionNames(entity.getRoles()))")
    Administrator toDomain(AdministratorEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "roles", ignore = true)
    AdministratorEntity toEntity(Administrator administrator);

    default Set<String> mapRoleNames(Set<RoleEntity> roles) {
        return roles == null ? Set.of() : roles.stream().map(RoleEntity::getName).collect(Collectors.toSet());
    }

    default Set<String> mapPermissionNames(Set<RoleEntity> roles) {
        return roles == null
                ? Set.of()
                : roles.stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(PermissionEntity::getName)
                        .collect(Collectors.toSet());
    }
}
