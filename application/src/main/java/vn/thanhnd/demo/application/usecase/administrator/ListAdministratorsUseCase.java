package vn.thanhnd.demo.application.usecase.administrator;

import org.springframework.transaction.annotation.Transactional;
import vn.thanhnd.demo.application.base.ResultHandler;
import vn.thanhnd.demo.application.base.ResultWrapper;
import vn.thanhnd.demo.domain.adapter.AdministratorRepositoryPort;
import vn.thanhnd.demo.domain.model.Administrator;
import vn.thanhnd.demo.util.annotation.UseCase;

import java.util.List;

/**
 * Lists all administrator accounts. Test/demo endpoint for permission-based authorization
 * (requires the {@code ADMINISTRATOR_READ} permission).
 */
@UseCase
public class ListAdministratorsUseCase {

    private final AdministratorRepositoryPort administratorRepositoryPort;

    public ListAdministratorsUseCase(AdministratorRepositoryPort administratorRepositoryPort) {
        this.administratorRepositoryPort = administratorRepositoryPort;
    }

    /**
     * Execute list administrators operation.
     * <p>
     * Orchestrates the following steps:
     * 1. Retrieve all administrators from the repository (read-only transaction, routed to a replica)
     * 2. Map each Administrator domain model to an AdministratorSummaryResponse
     *
     * @return ResultWrapper with the list of AdministratorSummaryResponse on success
     */
    @Transactional(readOnly = true)
    public ResultWrapper<List<AdministratorSummaryResponse>> list() {
        return ResultHandler.handle(() -> administratorRepositoryPort.findAll().stream()
                .map(ListAdministratorsUseCase::toSummary)
                .toList());
    }

    private static AdministratorSummaryResponse toSummary(Administrator administrator) {
        return new AdministratorSummaryResponse(
                administrator.id(),
                administrator.username(),
                administrator.email(),
                administrator.status().name(),
                administrator.roleNames());
    }
}
