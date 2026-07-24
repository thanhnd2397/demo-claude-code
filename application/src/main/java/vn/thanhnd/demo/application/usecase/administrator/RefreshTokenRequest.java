package vn.thanhnd.demo.application.usecase.administrator;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(@NotBlank String refreshToken) {
}
