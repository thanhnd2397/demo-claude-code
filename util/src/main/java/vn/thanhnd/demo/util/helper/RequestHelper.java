package vn.thanhnd.demo.util.helper;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Request inspection helper (e.g. HTMX detection). Inject this interface, not the implementation.
 */
public interface RequestHelper {

    boolean isHtmxRequest(HttpServletRequest request);
}
