package vn.thanhnd.demo.presentation.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.thanhnd.demo.application.usecase.administrator.AdministratorSummaryResponse;
import vn.thanhnd.demo.application.usecase.administrator.ListAdministratorsUseCase;
import vn.thanhnd.demo.application.usecase.administrator.UpdateAdministratorRolesRequest;
import vn.thanhnd.demo.application.usecase.administrator.UpdateAdministratorRolesUseCase;
import vn.thanhnd.demo.application.usecase.administrator.UpdateAdministratorStatusRequest;
import vn.thanhnd.demo.application.usecase.administrator.UpdateAdministratorStatusUseCase;
import vn.thanhnd.demo.util.helper.ResponseMaker;
import vn.thanhnd.demo.util.response.RestResponse;

import java.util.List;

/**
 * Test/demo endpoints exercising both role-based and permission-based authorization on top of
 * {@code m_administrators}: {@link #list} and {@link #updateRoles} require a permission
 * ({@code ADMINISTRATOR_READ}/{@code ADMINISTRATOR_MANAGE}), while {@link #updateStatus} requires
 * the {@code ADMIN} role directly, for contrast.
 */
@RestController
@RequestMapping("/administrators")
public class AdministratorController extends BaseController {

    private final ListAdministratorsUseCase listAdministratorsUseCase;
    private final UpdateAdministratorRolesUseCase updateAdministratorRolesUseCase;
    private final UpdateAdministratorStatusUseCase updateAdministratorStatusUseCase;

    public AdministratorController(
            ListAdministratorsUseCase listAdministratorsUseCase,
            UpdateAdministratorRolesUseCase updateAdministratorRolesUseCase,
            UpdateAdministratorStatusUseCase updateAdministratorStatusUseCase,
            ResponseMaker responseMaker) {
        super(responseMaker);
        this.listAdministratorsUseCase = listAdministratorsUseCase;
        this.updateAdministratorRolesUseCase = updateAdministratorRolesUseCase;
        this.updateAdministratorStatusUseCase = updateAdministratorStatusUseCase;
    }

    /**
     * List all administrators.
     * GET /api/v1/administrators
     * <p>
     * Retrieves every administrator account with its id, username, email, status, and role names.
     * No filtering or pagination — test/demo endpoint for permission-based authorization.
     * Requires Bearer token carrying the ADMINISTRATOR_READ permission (403 otherwise).
     *
     * @return RestResponse with the list of AdministratorSummaryResponse
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMINISTRATOR_READ')")
    public ResponseEntity<RestResponse<List<AdministratorSummaryResponse>>> list() {
        return toResponseEntity(listAdministratorsUseCase.list(), HttpStatus.OK);
    }

    /**
     * Replace an administrator's role assignments.
     * PATCH /api/v1/administrators/{id}/roles
     * <p>
     * Replaces the target administrator's entire role set with the requested role names —
     * a full replace, not an additive merge. Already-issued JWTs are unaffected until the
     * target re-logs-in or refreshes.
     * Requires Bearer token carrying the ADMINISTRATOR_MANAGE permission (403 otherwise).
     *
     * @param id The target administrator's id
     * @param request UpdateAdministratorRolesRequest containing the complete new set of role names (non-empty)
     * @return RestResponse with the updated AdministratorSummaryResponse
     */
    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('ADMINISTRATOR_MANAGE')")
    public ResponseEntity<RestResponse<AdministratorSummaryResponse>> updateRoles(
            @PathVariable String id, @Valid @RequestBody UpdateAdministratorRolesRequest request) {
        return toResponseEntity(updateAdministratorRolesUseCase.updateRoles(id, request), HttpStatus.OK);
    }

    /**
     * Change an administrator's lifecycle status.
     * PATCH /api/v1/administrators/{id}/status
     * <p>
     * Sets the target administrator's status to the requested value (ACTIVE or INACTIVE).
     * An INACTIVE account can no longer log in or refresh tokens.
     * Requires Bearer token carrying the ADMIN role (role-based check, for contrast with the
     * permission-based endpoints above; 403 otherwise).
     *
     * @param id The target administrator's id
     * @param request UpdateAdministratorStatusRequest containing the new status
     * @return RestResponse with the updated AdministratorSummaryResponse
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<AdministratorSummaryResponse>> updateStatus(
            @PathVariable String id, @Valid @RequestBody UpdateAdministratorStatusRequest request) {
        return toResponseEntity(updateAdministratorStatusUseCase.updateStatus(id, request), HttpStatus.OK);
    }
}
