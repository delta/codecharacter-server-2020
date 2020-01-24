package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.request.PublicUserRequest;
import delta.codecharacter.server.controller.request.RegisterUserRequest;
import delta.codecharacter.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    private final Logger LOG = Logger.getLogger(UserController.class.getName());

    @Autowired
    private UserService userService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<String> registerUser(@RequestBody @Valid RegisterUserRequest user) {
        userService.registerUser(user);
        return new ResponseEntity<>("User Registered Successfully!", HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<PublicUserRequest> getAllUsers() {
        return userService.getAllUsers();
    }
}
