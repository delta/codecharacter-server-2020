package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.request.User.PasswordResetRequest;
import delta.codecharacter.server.controller.request.User.PublicUserRequest;
import delta.codecharacter.server.controller.request.User.RegisterUserRequest;
import delta.codecharacter.server.controller.response.UserMatchStatsResponse;
import delta.codecharacter.server.controller.response.UserRatingsResponse;
import delta.codecharacter.server.service.MatchService;
import delta.codecharacter.server.service.UserRatingService;
import delta.codecharacter.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.logging.Logger;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/user")
public class UserController {

    private final Logger LOG = Logger.getLogger(UserController.class.getName());

    @Autowired
    private UserService userService;

    @Autowired
    private UserRatingService userRatingService;

    @Autowired
    private MatchService matchService;

    @PostMapping(value = "")
    public ResponseEntity<String> registerUser(@RequestBody @Valid RegisterUserRequest user) {
        userService.registerUser(user);
        return new ResponseEntity<>("User Registration Successful!", HttpStatus.CREATED);
    }

    @GetMapping(value = "")
    public ResponseEntity<List<PublicUserRequest>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @GetMapping(value = "/match-stats/{username}")
    public ResponseEntity<UserMatchStatsResponse> getUserMatchStats(@PathVariable @NotEmpty String username) {
        return new ResponseEntity<>(matchService.getUserMatchStats(username), HttpStatus.OK);
    }

    @GetMapping(value = "/wait-time/{username}")
    public ResponseEntity<Long> getWaitTime(@PathVariable @NotEmpty String username) {
        return new ResponseEntity<>(matchService.getWaitTime(username), HttpStatus.OK);
    }

    @PostMapping(value = "/activate")
    public ResponseEntity<String> activateUser(@RequestBody String authToken) {
        userService.activateUser(authToken);
        return new ResponseEntity<>("Account Activation Successful!", HttpStatus.OK);
    }

    @PostMapping(value = "/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody String email) {
        userService.sendPasswordResetLink(email);
        return new ResponseEntity<>("Password Reset URL sent to the registered email!", HttpStatus.OK);
    }

    @PostMapping(value = "/password")
    public ResponseEntity<String> changePassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest) {
        userService.changePassword(passwordResetRequest);
        return new ResponseEntity<>("Password Changed Successfully!", HttpStatus.OK);
    }

    //Get all the Ratings of a User from the beginning to the current rating.
    @GetMapping(value = "/ratings/{username}")
    public ResponseEntity<List<UserRatingsResponse>> getUserRatings(@PathVariable String username) {
        return new ResponseEntity<List<UserRatingsResponse>>(userRatingService.getUserRatings(username), HttpStatus.OK);
    }
}
