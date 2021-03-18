package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.response.GameLogs;
import delta.codecharacter.server.service.GameService;
import delta.codecharacter.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/log/{gameId}")
    public ResponseEntity<GameLogs> getGameLog(@PathVariable Integer gameId, Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        var user = userService.getUserByEmail(email);
        return new ResponseEntity<>(gameService.getGameLog(gameId, user.getUserId()), HttpStatus.OK);
    }
}
