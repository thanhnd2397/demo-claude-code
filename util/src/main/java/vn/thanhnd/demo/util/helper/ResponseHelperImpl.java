package vn.thanhnd.demo.util.helper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Component
public class ResponseHelperImpl implements ResponseHelper {

    private static final String SESSION_MESSAGE_KEY = "sessionMessage";

    private final RequestHelper requestHelper;

    public ResponseHelperImpl(RequestHelper requestHelper) {
        this.requestHelper = requestHelper;
    }

    @Override
    public String resolveView(HttpServletRequest request, String fullView, String fragmentView) {
        return requestHelper.isHtmxRequest(request) ? fragmentView : fullView;
    }

    @Override
    public ResponseEntity<Void> createRedirectResponse(HttpServletRequest request, String url) {
        if (!requestHelper.isHtmxRequest(request)) {
            return null;
        }
        return ResponseEntity.ok().header("HX-Redirect", url).build();
    }

    @Override
    public Object handleRedirect(HttpServletRequest request, String url, RedirectAttributes redirectAttributes) {
        if (requestHelper.isHtmxRequest(request)) {
            return createRedirectResponse(request, url);
        }
        return "redirect:" + url;
    }

    @Override
    public Object handleRedirectWithSessionMessage(HttpServletRequest request, String url, String message,
            RedirectAttributes redirectAttributes) {
        if (requestHelper.isHtmxRequest(request)) {
            HttpSession session = request.getSession();
            session.setAttribute(SESSION_MESSAGE_KEY, message);
            return createRedirectResponse(request, url);
        }
        redirectAttributes.addFlashAttribute(SESSION_MESSAGE_KEY, message);
        return "redirect:" + url;
    }
}
