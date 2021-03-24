package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.model.User;
import delta.codecharacter.server.service.HomeService;
import delta.codecharacter.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    UserService userService;

    @Autowired
    HomeService homeService;

    @GetMapping(value = "/")
    public String getHomeString(Authentication authentication) {
        if (authentication == null)
            return "Invalid Login";
        return homeService.getHomeString();
    }
}
