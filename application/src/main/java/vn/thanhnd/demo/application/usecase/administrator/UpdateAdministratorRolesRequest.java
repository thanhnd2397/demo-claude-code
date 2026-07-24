package vn.thanhnd.demo.application.usecase.administrator;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateAdministratorRolesRequest(@NotEmpty Set<String> roleNames) {
}
