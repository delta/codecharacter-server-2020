package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.request.User.*;
import delta.codecharacter.server.controller.response.Match.DetailedMatchStatsResponse;
import delta.codecharacter.server.controller.response.Match.PrivateMatchResponse;
import delta.codecharacter.server.controller.response.User.PrivateUserResponse;
import delta.codecharacter.server.controller.response.User.PublicUserResponse;
import delta.codecharacter.server.controller.response.UserRatingsResponse;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.service.MatchService;
import delta.codecharacter.server.service.UserRatingService;
import delta.codecharacter.server.service.UserService;
import delta.codecharacter.server.util.enums.AuthMethod;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<String> registerUser(@RequestBody @Valid RegisterUserRequest registerUserRequest) {
        if (userService.isEmailPresent(registerUserRequest.getEmail()))
            return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
        if (userService.isUsernamePresent(registerUserRequest.getUsername()))
            return new ResponseEntity<>("Username already exists", HttpStatus.CONFLICT);
        userService.registerUser(registerUserRequest);
        return new ResponseEntity<>("User Registration Successful!", HttpStatus.CREATED);
    }

    @PatchMapping(value = "")
    public ResponseEntity<String> updateUser(@RequestBody @Valid UpdateUserRequest updateUserRequest, Authentication authentication) {
        User user = userService.getUserByEmail(userService.getEmailFromAuthentication(authentication));
        if (user == null)
            return new ResponseEntity<>("Invalid Login", HttpStatus.UNAUTHORIZED);
        if (updateUserRequest.getUsername() != null && userService.isUsernamePresent(updateUserRequest.getUsername()))
            return new ResponseEntity<>("Username already exists", HttpStatus.CONFLICT);
        userService.updateUser(user.getEmail(), updateUserRequest);
        return new ResponseEntity<>("User Account Updated Successfully!", HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "")
    public ResponseEntity<PrivateUserResponse> getPrivateUser(Authentication authentication) {
        User user = userService.getUserByEmail(userService.getEmailFromAuthentication(authentication));
        if (user == null)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(userService.getPrivateUser(user.getUserId()), HttpStatus.OK);
    }

    @GetMapping(value = "/{username}")
    public ResponseEntity<PublicUserResponse> getPublicUser(@PathVariable @NotEmpty String username) {
        if (!userService.isUsernamePresent(username))
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(userService.getPublicUser(username), HttpStatus.OK);
    }

    //route to enable authenticated user to change their password
    @PatchMapping(value = "/password")
    public ResponseEntity<String> updatePassword(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest, Authentication authentication) {
        User user = userService.getUserByEmail(userService.getEmailFromAuthentication(authentication));
        if (user == null)
            return new ResponseEntity<>("Invalid Login", HttpStatus.UNAUTHORIZED);

        // PRAGYAN and SSO authType users should not be allowed to update password
        if (user.getAuthMethod() != AuthMethod.MANUAL)
            return new ResponseEntity<>("Auth type not supported", HttpStatus.FORBIDDEN);

        if (!bCryptPasswordEncoder.matches(updatePasswordRequest.getOldPassword(), user.getPassword()))
            return new ResponseEntity<>("Incorrect old password", HttpStatus.UNAUTHORIZED);

        userService.updatePassword(user.getEmail(), updatePasswordRequest.getNewPassword());
        return new ResponseEntity<>("User Password Updated Successfully!", HttpStatus.OK);
    }

    @GetMapping(value = "/match-stats/{username}")
    public ResponseEntity<DetailedMatchStatsResponse> getUserMatchStats(@PathVariable @NotEmpty String username) {
        if (!userService.isUsernamePresent(username))
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(matchService.getDetailedMatchStatsByUsername(username), HttpStatus.OK);
    }

    @GetMapping(value = "/wait-time")
    public ResponseEntity<Long> getWaitTime(Authentication authentication) {
        User user = userService.getUserByEmail(userService.getEmailFromAuthentication(authentication));
        if (user == null)
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(matchService.getWaitTime(user.getUserId()), HttpStatus.OK);
    }

    @RequestMapping(value = "/email/{email}", method = RequestMethod.HEAD)
    public ResponseEntity<HttpStatus> checkUserExistsByEmail(@PathVariable String email) {
        // If email exists, return FOUND, else return NOT_FOUND
        if (!userService.isEmailPresent(email))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(HttpStatus.FOUND);
    }

    @RequestMapping(value = "/username/{username}", method = RequestMethod.HEAD)
    public ResponseEntity<HttpStatus> checkUserExistsByUsername(@PathVariable @NotEmpty String username) {
        // If email exists, return FOUND, else return NOT_FOUND
        if (!userService.isUsernamePresent(username))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(HttpStatus.FOUND);
    }

    @PostMapping(value = "/activate")
    public ResponseEntity<String> activateUser(@RequestBody @Valid ActivateUserRequest activateUserRequest) {
        return new ResponseEntity<>(userService.activateUser(activateUserRequest), HttpStatus.OK);
    }

    @PostMapping(value = "/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody @NotEmpty String email) {
        if (!userService.isEmailPresent(email))
            return new ResponseEntity<>("Invalid email", HttpStatus.NOT_FOUND);
        userService.sendPasswordResetLink(email);
        return new ResponseEntity<>("Password Reset URL sent to the registered email!", HttpStatus.OK);
    }

    // Route to enable user to reset password after getting passwordResetToken from /forgot-password
    @PostMapping(value = "/password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest) {
        return new ResponseEntity<>(userService.resetPassword(passwordResetRequest), HttpStatus.OK);
    }

    // Get all the Ratings of a User from the beginning to the current rating.
    @GetMapping(value = "/ratings/{username}")
    public ResponseEntity<List<UserRatingsResponse>> getUserRatings(@PathVariable String username) {
        return new ResponseEntity<List<UserRatingsResponse>>(userRatingService.getUserRatings(username), HttpStatus.OK);
    }

    @GetMapping(value = "/match/{pageNo}/{pageSize}")
    public ResponseEntity<List<PrivateMatchResponse>> getManualAndAutoExecutedMatches(@PathVariable Integer pageNo, @PathVariable Integer pageSize, Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return new ResponseEntity<>(matchService.getManualAndAutoExecutedMatchesPaginated(user.getUserId(), pageable), HttpStatus.OK);
    }

    public ResponseEntity<String> updateLevel(Authentication authentication){
        String email = userService.getEmailFromAuthentication(authentication);
        return new ResponseEntity<String>(userService.updateLevel(email),HttpStatus.OK);
    }
}
