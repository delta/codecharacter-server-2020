package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.request.User.*;
import delta.codecharacter.server.controller.response.User.PrivateUserResponse;
import delta.codecharacter.server.controller.response.User.PublicUserResponse;
import delta.codecharacter.server.controller.response.UserMatchStatsResponse;
import delta.codecharacter.server.controller.response.UserRatingsResponse;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.service.MatchService;
import delta.codecharacter.server.service.UserRatingService;
import delta.codecharacter.server.service.UserService;
import delta.codecharacter.server.util.enums.AuthMethod;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping(value = "")
    public ResponseEntity<String> registerUser(@RequestBody @Valid RegisterUserRequest user) {
        if (userService.isEmailPresent(user.getEmail()))
            return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
        userService.registerUser(user);
        return new ResponseEntity<>("User Registration Successful!", HttpStatus.CREATED);
    }

    @PutMapping(value = "")
    public ResponseEntity<String> updateUser(@RequestBody @Valid UpdateUserRequest updateUserRequest, Authentication authentication) {
        User user = userService.getUserByEmail(userService.getEmailFromAuthentication(authentication));
        if (user == null)
            return new ResponseEntity<>("Invalid Login", HttpStatus.UNAUTHORIZED);
        userService.updateUser(user.getEmail(), updateUserRequest);
        return new ResponseEntity<>("User Account Updation Successful!", HttpStatus.OK);
    }

    //route to enable authenticated user to change their password
    @PatchMapping(value = "/password")
    public ResponseEntity<String> updatePassword(@RequestBody @NotEmpty UpdatePasswordRequest updatePasswordRequest, Authentication authentication) {
        User user = userService.getUserByEmail(userService.getEmailFromAuthentication(authentication));

        if (user == null)
            return new ResponseEntity<>("Invalid Login", HttpStatus.UNAUTHORIZED);

        if (user.getAuthMethod() != AuthMethod.MANUAL)
            return new ResponseEntity<>("Auth type not supported", HttpStatus.UNAUTHORIZED);

        if (!user.getPassword().matches(bCryptPasswordEncoder.encode(updatePasswordRequest.getOldPassword())))
            return new ResponseEntity<>("Incorrect old password", HttpStatus.UNAUTHORIZED);

        userService.updatePassword(user.getEmail(), updatePasswordRequest.getNewPassword());
        return new ResponseEntity<>("User Password Updation Successful!", HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "")
    public ResponseEntity<PrivateUserResponse> getPrivateUser(Authentication authentication) {
        User user = userService.getUserByEmail(userService.getEmailFromAuthentication(authentication));
        if (user == null)
            throw new Exception("Invalid Login");
        return new ResponseEntity<>(userService.getPrivateUser(user.getUserId()), HttpStatus.OK);
    }

    @GetMapping(value = "/username/{username}")
    public ResponseEntity<PublicUserResponse> getPublicUser(String username) {
        return new ResponseEntity<>(userService.getPublicUser(username), HttpStatus.OK);
    }

    @GetMapping(value = "/match-stats/{userId}")
    public ResponseEntity<UserMatchStatsResponse> getUserMatchStats(@PathVariable @NotEmpty Integer userId) {
        return new ResponseEntity<>(matchService.getUserMatchStats(userId), HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "/wait-time")
    public ResponseEntity<Long> getWaitTime(Authentication authentication) {
        User user = userService.getUserByEmail(userService.getEmailFromAuthentication(authentication));
        if (user == null)
            throw new Exception("Invalid Login");
        return new ResponseEntity<>(matchService.getWaitTime(user.getUserId()), HttpStatus.OK);
    }

    @RequestMapping(value = "/email/{email}", method = RequestMethod.HEAD)
    public ResponseEntity<HttpStatus> checkUserExistsByEmail(@PathVariable String email) {
        Boolean exists = userService.isEmailPresent(email);

        // If email exists, return FOUND, else return NOT_FOUND
        if (!exists)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(HttpStatus.FOUND);
    }

    @PostMapping(value = "/activate")
    public ResponseEntity<String> activateUser(@RequestBody ActivateUserRequest activateUserRequest) {
        return new ResponseEntity<>(userService.activateUser(activateUserRequest), HttpStatus.OK);
    }

    @PostMapping(value = "/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody String email) {
        userService.sendPasswordResetLink(email);
        return new ResponseEntity<>("Password Reset URL sent to the registered email!", HttpStatus.OK);
    }

    //route to enable user to reset password after getting passwordResetToken from /forgot-password
    @PostMapping(value = "/password")
    public ResponseEntity<String> changePassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest) {
        userService.changePassword(passwordResetRequest);
        return new ResponseEntity<>("Password Changed Successfully!", HttpStatus.OK);
    }

    //Get all the Ratings of a User from the beginning to the current rating.
    @GetMapping(value = "/ratings/{userId}")
    public ResponseEntity<List<UserRatingsResponse>> getUserRatings(@PathVariable Integer userId) {
        return new ResponseEntity<List<UserRatingsResponse>>(userRatingService.getUserRatings(userId), HttpStatus.OK);
    }
}
