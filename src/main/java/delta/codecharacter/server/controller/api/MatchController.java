package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.request.Match.UpdateMatchRequest;
import delta.codecharacter.server.service.MatchService;
import delta.codecharacter.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/match")
public class MatchController {

    @Autowired
    MatchService matchService;

    @Autowired
    UserService userService;

    @PostMapping("/update")
    public ResponseEntity<String> updateMatch(@RequestBody UpdateMatchRequest updateMatchRequest, Authentication authentication) {
        userService.getUserByUsername(authentication.getName());
        matchService.updateMatch(updateMatchRequest);
        return new ResponseEntity<>("Updated successfully", HttpStatus.OK);
    }
}
