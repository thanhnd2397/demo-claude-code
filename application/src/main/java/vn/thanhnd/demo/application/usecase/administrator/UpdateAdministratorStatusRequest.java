package vn.thanhnd.demo.application.usecase.administrator;

import jakarta.validation.constraints.NotNull;
import vn.thanhnd.demo.domain.enums.AdministratorStatus;

public record UpdateAdministratorStatusRequest(@NotNull AdministratorStatus status) {
}
