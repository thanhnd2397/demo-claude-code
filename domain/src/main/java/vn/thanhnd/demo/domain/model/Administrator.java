package vn.thanhnd.demo.domain.model;

import vn.thanhnd.demo.domain.enums.AdministratorStatus;

import java.util.Set;

/**
 * An administrator account. {@code passwordHash} is never exposed outside the auth flow.
 * No validation here — the use case validates uniqueness and format at write time.
 */
public record Administrator(
        String id,
        String username,
        String email,
        String passwordHash,
        AdministratorStatus status,
        Set<String> roleNames,
        Set<String> permissionNames) {

    public static Administrator of(
            String id,
            String username,
            String email,
            String passwordHash,
            AdministratorStatus status,
            Set<String> roleNames,
            Set<String> permissionNames) {
        return new Administrator(id, username, email, passwordHash, status, roleNames, permissionNames);
    }
}
