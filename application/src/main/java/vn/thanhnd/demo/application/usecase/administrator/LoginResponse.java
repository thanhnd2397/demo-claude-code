package vn.thanhnd.demo.application.usecase.administrator;

public record LoginResponse(String accessToken, String refreshToken, long expiresInSeconds) {
}
