package vn.thanhnd.demo.presentation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.thanhnd.demo.presentation.filter.JwtAuthenticationFilter;
import vn.thanhnd.demo.presentation.handler.RestAccessDeniedHandler;
import vn.thanhnd.demo.presentation.handler.RestAuthenticationEntryPoint;

/**
 * Stateless JWT security: no sessions, no CSRF (not applicable to a bearer-token API),
 * {@code /auth/login}, {@code /auth/register}, {@code /auth/refresh}, the Swagger/OpenAPI paths,
 * and {@code /error} are open (reachable at {@code /api/v1/*} once {@code server.servlet.context-path}
 * is applied), everything else requires a valid access token. {@code /error} must stay permitted:
 * any unhandled MVC exception triggers a servlet-container forward to it, which re-enters this
 * filter chain as a new dispatch — without this rule that forward gets rejected as unauthenticated,
 * masking the real error behind a misleading 401. Method-level
 * {@code @PreAuthorize("hasAuthority('ROLE_NAME')")} is available on any endpoint once this is wired
 * in.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login", "/auth/register", "/auth/refresh",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
