package vn.thanhnd.demo.presentation.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.thanhnd.demo.util.helper.DateTimeParser;

/**
 * Home page — proves the Thymeleaf + Tailwind + HTMX wiring end-to-end.
 */
@Controller
public class HomeController {

    private final DateTimeParser dateTimeParser;

    public HomeController(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/ping")
    public String ping(Model model) {
        model.addAttribute("timestamp", dateTimeParser.format(dateTimeParser.getCurrentJapanDateTime()));
        return "fragments/ping :: pong";
    }
}
