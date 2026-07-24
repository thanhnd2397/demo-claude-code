package vn.thanhnd.demo.domain.model;

/**
 * A single grantable capability (e.g. {@code ADMINISTRATOR_CREATE}), assigned to {@link Role}s.
 */
public record Permission(String id, String name, String description) {

    public static Permission of(String id, String name, String description) {
        return new Permission(id, name, description);
    }
}
