package vn.thanhnd.demo.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.jwt.*} properties: signing secret and token lifetimes.
 */
@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpirationMinutes = 30;
    private long refreshTokenExpirationMinutes = 43200;
}
