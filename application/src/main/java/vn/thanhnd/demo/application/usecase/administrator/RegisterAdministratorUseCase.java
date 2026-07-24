package vn.thanhnd.demo.application.usecase.administrator;

import org.springframework.transaction.annotation.Transactional;
import vn.thanhnd.demo.application.base.ResultHandler;
import vn.thanhnd.demo.application.base.ResultWrapper;
import vn.thanhnd.demo.domain.adapter.AdministratorRepositoryPort;
import vn.thanhnd.demo.domain.adapter.PasswordHasher;
import vn.thanhnd.demo.domain.adapter.RoleRepositoryPort;
import vn.thanhnd.demo.domain.enums.AdministratorStatus;
import vn.thanhnd.demo.domain.exception.DomainValidationException;
import vn.thanhnd.demo.domain.model.Administrator;
import vn.thanhnd.demo.domain.model.Role;
import vn.thanhnd.demo.util.annotation.UseCase;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a new administrator account with the default {@code ADMIN} role, ACTIVE status,
 * and a BCrypt-hashed password.
 */
@UseCase
public class RegisterAdministratorUseCase {

    private static final String DEFAULT_ROLE_NAME = "ADMIN";

    private final AdministratorRepositoryPort administratorRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final PasswordHasher passwordHasher;

    public RegisterAdministratorUseCase(
            AdministratorRepositoryPort administratorRepositoryPort,
            RoleRepositoryPort roleRepositoryPort,
            PasswordHasher passwordHasher) {
        this.administratorRepositoryPort = administratorRepositoryPort;
        this.roleRepositoryPort = roleRepositoryPort;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Execute register administrator operation.
     * <p>
     * Orchestrates the following steps:
     * 1. Validate the username is not already taken (E-01-ADMINISTRATOR-0001)
     * 2. Validate the email is not already taken (E-01-ADMINISTRATOR-0002)
     * 3. Load the default ADMIN role and derive its role and permission names
     * 4. Build the Administrator domain model with ACTIVE status and a BCrypt-hashed password
     * 5. Persist the administrator and return its id, username, and email
     *
     * @param request RegisterAdministratorRequest containing username, email, and password
     * @return ResultWrapper with RegisterAdministratorResponse on success, or the domain error code on failure
     */
    @Transactional
    public ResultWrapper<RegisterAdministratorResponse> register(RegisterAdministratorRequest request) {
        return ResultHandler.handle(() -> {
            if (administratorRepositoryPort.existsByUsername(request.username())) {
                throw new DomainValidationException("E-01-ADMINISTRATOR-0001");
            }
            if (administratorRepositoryPort.existsByEmail(request.email())) {
                throw new DomainValidationException("E-01-ADMINISTRATOR-0002");
            }

            Set<Role> defaultRoles = roleRepositoryPort.findByNames(Set.of(DEFAULT_ROLE_NAME));
            Set<String> roleNames = defaultRoles.stream().map(Role::name).collect(Collectors.toSet());
            Set<String> permissionNames = defaultRoles.stream()
                    .flatMap(role -> role.permissionNames().stream())
                    .collect(Collectors.toSet());

            Administrator administrator = Administrator.of(
                    null,
                    request.username(),
                    request.email(),
                    passwordHasher.hash(request.password()),
                    AdministratorStatus.ACTIVE,
                    roleNames,
                    permissionNames);

            Administrator saved = administratorRepositoryPort.save(administrator);
            return new RegisterAdministratorResponse(saved.id(), saved.username(), saved.email());
        });
    }
}
