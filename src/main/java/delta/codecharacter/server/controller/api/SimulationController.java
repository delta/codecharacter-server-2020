package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.request.Simulation.SimulateMatchRequest;
import delta.codecharacter.server.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.logging.Logger;

@Controller
public class SimulationController {

    private final Logger LOG = Logger.getLogger(SimulationController.class.getName());

    @Autowired
    private SimulationService simulationService;

    @MessageMapping("/matchRequest")
    @SendTo("/socket-response/simulation/{userId}")
    public void simulateMatch(@RequestBody @Valid SimulateMatchRequest simulateMatchRequest) {
        simulationService.simulateMatch(simulateMatchRequest);
    }

    public String sendSocketMessage(@DestinationVariable @NotEmpty Integer userId, @NotEmpty String message) {
        return message;
    }
}
