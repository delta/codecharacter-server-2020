package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.request.Match.ExecuteMatchRequest;
import delta.codecharacter.server.service.ConstantService;
import delta.codecharacter.server.service.MatchService;
import delta.codecharacter.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/match")
public class MatchController {

    @Autowired
    MatchService matchService;

    @Autowired
    UserService userService;

    @Autowired
    ConstantService constantService;

    @Value("$compilebox.secret-key")
    private String key;

    @PutMapping("/")
    public ResponseEntity<String> updateMatch(@RequestBody @Valid ExecuteMatchRequest executeMatchRequest, Authentication authentication) {
        // TODO: Check if the key sent by compilebox matches with the server key.
        matchService.executeMatch(executeMatchRequest);
        return new ResponseEntity<>("Updated successfully", HttpStatus.OK);
    }
}
