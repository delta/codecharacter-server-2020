package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.response.UserStatsResponse;
import delta.codecharacter.server.service.ConstantService;
import delta.codecharacter.server.service.UserService;
import delta.codecharacter.server.service.UserStatsService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/user-stats")
public class UserStatsController {

    private final Logger LOG = Logger.getLogger(UserStatsController.class.getName());

    @Autowired
    private UserStatsService userStatsService;

    @Autowired
    private ConstantService constantService;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/{username}")
    public ResponseEntity<UserStatsResponse> getUserStats(@PathVariable @NotEmpty String username) {
        return new ResponseEntity<UserStatsResponse>(userStatsService.getUserStats(username), HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = "/timer/{userName}")
    public ResponseEntity<String> getWaitTime(@PathVariable @NotEmpty String userName) {
        return new ResponseEntity<String>(userStatsService.getWaitTime(userName),HttpStatus.OK);
    }

}
