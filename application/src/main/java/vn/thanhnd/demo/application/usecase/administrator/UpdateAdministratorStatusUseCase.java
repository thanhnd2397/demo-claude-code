package vn.thanhnd.demo.application.usecase.administrator;

import org.springframework.transaction.annotation.Transactional;
import vn.thanhnd.demo.application.base.ResultHandler;
import vn.thanhnd.demo.application.base.ResultWrapper;
import vn.thanhnd.demo.domain.adapter.AdministratorRepositoryPort;
import vn.thanhnd.demo.domain.model.Administrator;
import vn.thanhnd.demo.util.annotation.UseCase;

/**
 * Changes an administrator's lifecycle status. Test/demo endpoint for role-based authorization
 * (requires the {@code ADMIN} role, as a contrast to the permission-based endpoints above).
 */
@UseCase
public class UpdateAdministratorStatusUseCase {

    private final AdministratorRepositoryPort administratorRepositoryPort;

    public UpdateAdministratorStatusUseCase(AdministratorRepositoryPort administratorRepositoryPort) {
        this.administratorRepositoryPort = administratorRepositoryPort;
    }

    /**
     * Execute update administrator status operation.
     * <p>
     * Orchestrates the following steps:
     * 1. Find the target administrator by id (E-01-ADMINISTRATOR-0010 if absent)
     * 2. Set the administrator's status to the requested value
     * 3. Return the updated administrator summary
     *
     * @param administratorId The target administrator's id
     * @param request UpdateAdministratorStatusRequest containing the new status
     * @return ResultWrapper with the updated AdministratorSummaryResponse on success, or the domain error code on failure
     */
    @Transactional
    public ResultWrapper<AdministratorSummaryResponse> updateStatus(
            String administratorId, UpdateAdministratorStatusRequest request) {
        return ResultHandler.handle(() -> {
            Administrator updated = administratorRepositoryPort.updateStatus(administratorId, request.status());
            return new AdministratorSummaryResponse(
                    updated.id(), updated.username(), updated.email(), updated.status().name(), updated.roleNames());
        });
    }
}
