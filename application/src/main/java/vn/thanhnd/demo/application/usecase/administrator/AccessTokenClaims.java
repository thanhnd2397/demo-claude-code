package vn.thanhnd.demo.application.usecase.administrator;

import java.util.Set;

/**
 * Decoded, verified access token claims returned to the presentation-layer security filter.
 */
public record AccessTokenClaims(String administratorId, Set<String> roleNames, Set<String> permissionNames) {
}
