package vn.thanhnd.demo.util.helper;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Resolves view names and redirects depending on whether the incoming request is an HTMX request.
 * Inject this interface, not the implementation.
 */
public interface ResponseHelper {

    String resolveView(HttpServletRequest request, String fullView, String fragmentView);

    ResponseEntity<Void> createRedirectResponse(HttpServletRequest request, String url);

    Object handleRedirect(HttpServletRequest request, String url, RedirectAttributes redirectAttributes);

    Object handleRedirectWithSessionMessage(HttpServletRequest request, String url, String message,
            RedirectAttributes redirectAttributes);
}
