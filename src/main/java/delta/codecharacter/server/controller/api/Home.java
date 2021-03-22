package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.model.User;
import delta.codecharacter.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Home {

    @Autowired
    UserService userService;

    @GetMapping(value = "/")
    public String getHomeString(Authentication authentication) {
        User user = userService.getUserByEmail(userService.getEmailFromAuthentication(authentication));
        if (user == null)
            return "Invalid Login";
        return "activation";
//        return new ResponseEntity<>("Hello", HttpStatus.OK);

    }
}
