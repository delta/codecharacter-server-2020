package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    HomeService homeService;

    @GetMapping(value = "/")
    public String getHomeString(Authentication authentication) {
        if (authentication == null)
            return "Invalid Login";
        return homeService.getHomeString();
    }
}
