package vn.thanhnd.demo.domain.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import vn.thanhnd.demo.domain.cache.CacheData;

import java.util.Set;

/**
 * Cached current role/permission names for an administrator, keyed by
 * {@code CACHE_ADMINISTRATOR_PERMISSIONS_{administratorId}}. Invalidated whenever the
 * administrator's roles or status change, so authorization checks reflect the current
 * database state instead of a JWT's stale embedded claims.
 */
@JsonTypeName("administratorPermissionsCacheData")
public record AdministratorPermissionsCacheData(Set<String> roleNames, Set<String> permissionNames)
        implements CacheData {
}
