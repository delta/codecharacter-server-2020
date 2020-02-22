package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.request.Simulation.SimulateMatchRequest;
import delta.codecharacter.server.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.logging.Logger;

@Controller
public class SimulationController {

    private final Logger LOG = Logger.getLogger(SimulationController.class.getName());

    @Autowired
    private SimulationService simulationService;

    @MessageMapping("/matchRequest")
    @SendTo("/socket/simulation/{userId}")
    public void simulateMatch(@RequestBody @Valid SimulateMatchRequest simulateMatchRequest) {
        simulationService.simulateMatch(simulateMatchRequest);
    }
}
