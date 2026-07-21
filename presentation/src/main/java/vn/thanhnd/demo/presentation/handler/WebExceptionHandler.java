package vn.thanhnd.demo.presentation.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import vn.thanhnd.demo.util.exception.CoreException;
import vn.thanhnd.demo.util.helper.RequestHelper;

/**
 * Handles exceptions raised under {@code presentation.view}: HTMX requests get an
 * {@code HX-Redirect} response, full-page requests get an error view.
 */
@ControllerAdvice(basePackages = "vn.thanhnd.demo.presentation.view")
@RequiredArgsConstructor
public class WebExceptionHandler {

    private final RequestHelper requestHelper;

    @ExceptionHandler(CoreException.class)
    public Object handleCore(CoreException e, HttpServletRequest request) {
        if (requestHelper.isHtmxRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("HX-Redirect", "/error")
                    .build();
        }
        ModelAndView mav = new ModelAndView("error/general");
        mav.addObject("message", e.getMessage());
        return mav;
    }
}
