package vn.thanhnd.demo.domain.adapter;

import vn.thanhnd.demo.domain.model.Role;

import java.util.Set;

/**
 * Port for {@link Role} lookups.
 */
public interface RoleRepositoryPort {

    /**
     * Find roles matching the given names.
     *
     * @param names The role names to look up
     * @return The roles found; names with no matching role are silently omitted
     */
    Set<Role> findByNames(Set<String> names);
}
