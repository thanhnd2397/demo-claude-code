package vn.thanhnd.demo.presentation.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.thanhnd.demo.application.base.ResultWrapper;
import vn.thanhnd.demo.application.usecase.administrator.AccessTokenClaims;
import vn.thanhnd.demo.application.usecase.administrator.ValidateAccessTokenUseCase;
import vn.thanhnd.demo.domain.exception.DomainError;
import vn.thanhnd.demo.util.helper.ResponseMaker;
import vn.thanhnd.demo.util.response.RestResponse;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Populates {@link SecurityContextHolder} from a {@code Bearer} access token. Requests with no
 * {@code Authorization} header are passed through unauthenticated, letting {@code SecurityConfig}'s
 * authorization rules decide whether the endpoint requires a token.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SERVICE_NAME = "api";

    private final ValidateAccessTokenUseCase validateAccessTokenUseCase;
    private final ResponseMaker responseMaker;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = header.substring(BEARER_PREFIX.length());
        ResultWrapper<AccessTokenClaims> result = validateAccessTokenUseCase.validate(accessToken);

        if (!result.isSuccess()) {
            DomainError error = result.getErrors().get(0);
            RestResponse<Void> body = responseMaker.createFailResponse(SERVICE_NAME, error.errorCode(), error.errorCode());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseMaker.writeResponse(response, body);
            return;
        }

        AccessTokenClaims claims = result.getData();
        Set<GrantedAuthority> authorities = Stream.concat(claims.roleNames().stream(), claims.permissionNames().stream())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(claims.administratorId(), null, authorities));

        filterChain.doFilter(request, response);
    }
}
