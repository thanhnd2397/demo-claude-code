package vn.thanhnd.demo.application.usecase.administrator;

import org.springframework.transaction.annotation.Transactional;
import vn.thanhnd.demo.application.base.ResultHandler;
import vn.thanhnd.demo.application.base.ResultWrapper;
import vn.thanhnd.demo.domain.adapter.AdministratorRepositoryPort;
import vn.thanhnd.demo.domain.model.Administrator;
import vn.thanhnd.demo.util.annotation.UseCase;

/**
 * Replaces an administrator's role assignments. Test/demo endpoint for permission-based
 * authorization (requires the {@code ADMINISTRATOR_MANAGE} permission).
 */
@UseCase
public class UpdateAdministratorRolesUseCase {

    private final AdministratorRepositoryPort administratorRepositoryPort;

    public UpdateAdministratorRolesUseCase(AdministratorRepositoryPort administratorRepositoryPort) {
        this.administratorRepositoryPort = administratorRepositoryPort;
    }

    /**
     * Execute update administrator roles operation.
     * <p>
     * Orchestrates the following steps:
     * 1. Find the target administrator by id (E-01-ADMINISTRATOR-0010 if absent)
     * 2. Resolve every requested role name to an existing role (E-01-ADMINISTRATOR-0011 on unknown names)
     * 3. Replace the administrator's entire role set with the requested roles (full replace, not a merge)
     * 4. Return the updated administrator summary
     *
     * @param administratorId The target administrator's id
     * @param request UpdateAdministratorRolesRequest containing the complete new set of role names
     * @return ResultWrapper with the updated AdministratorSummaryResponse on success, or the domain error code on failure
     */
    @Transactional
    public ResultWrapper<AdministratorSummaryResponse> updateRoles(
            String administratorId, UpdateAdministratorRolesRequest request) {
        return ResultHandler.handle(() -> {
            Administrator updated = administratorRepositoryPort.updateRoles(administratorId, request.roleNames());
            return new AdministratorSummaryResponse(
                    updated.id(), updated.username(), updated.email(), updated.status().name(), updated.roleNames());
        });
    }
}
