package vn.thanhnd.demo.util.helper;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class RequestHelperImpl implements RequestHelper {

    private static final String HTMX_HEADER = "HX-Request";

    @Override
    public boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader(HTMX_HEADER));
    }
}
