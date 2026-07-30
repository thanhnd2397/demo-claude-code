package vn.thanhnd.demo.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables {@code @Async} method execution (e.g. {@code LoginNotificationAdapterImpl}) using
 * Spring's default task executor.
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
}
