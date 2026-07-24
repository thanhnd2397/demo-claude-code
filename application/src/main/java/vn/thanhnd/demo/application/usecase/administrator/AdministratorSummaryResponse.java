package vn.thanhnd.demo.application.usecase.administrator;

import java.util.Set;

public record AdministratorSummaryResponse(
        String id, String username, String email, String status, Set<String> roleNames) {
}
