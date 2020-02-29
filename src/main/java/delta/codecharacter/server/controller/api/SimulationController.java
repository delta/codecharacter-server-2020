package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.request.Simulation.SimulateMatchRequest;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.service.SimulationService;
import delta.codecharacter.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.logging.Logger;

//@Controller
@RestController
@RequestMapping(value = "/simulate")
public class SimulationController {

    private final Logger LOG = Logger.getLogger(SimulationController.class.getName());

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private UserService userService;

    //    @MessageMapping("/match")
    @PostMapping("/match")
    public void simulateMatch(@RequestBody @Valid SimulateMatchRequest simulateMatchRequest, Authentication authentication) {
        User user = userService.getUserByEmail(userService.getEmailFromAuthentication(authentication));
        simulationService.simulateMatch(simulateMatchRequest, user.getUserId());
    }

}
