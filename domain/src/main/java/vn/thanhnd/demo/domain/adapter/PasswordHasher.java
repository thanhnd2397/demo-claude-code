package vn.thanhnd.demo.domain.adapter;

/**
 * Port for one-way password hashing (backed by BCrypt in infrastructure).
 */
public interface PasswordHasher {

    /**
     * Hash a raw password for storage.
     *
     * @param rawPassword The plain-text password
     * @return The one-way hash
     */
    String hash(String rawPassword);

    /**
     * Check a raw password against a stored hash.
     *
     * @param rawPassword The plain-text password to verify
     * @param passwordHash The stored hash to compare against
     * @return true if the password matches the hash
     */
    boolean matches(String rawPassword, String passwordHash);
}
