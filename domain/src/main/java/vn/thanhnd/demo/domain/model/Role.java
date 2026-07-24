package vn.thanhnd.demo.domain.model;

import java.util.Set;

/**
 * A named group of {@link Permission}s assignable to an {@link Administrator}.
 */
public record Role(String id, String name, String description, Set<String> permissionNames) {

    public static Role of(String id, String name, String description, Set<String> permissionNames) {
        return new Role(id, name, description, permissionNames);
    }
}
