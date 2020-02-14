package delta.codecharacter.server.controller.api;

import com.sun.xml.bind.v2.schemagen.xmlschema.Any;
import delta.codecharacter.server.controller.request.Simulation.SimulateMatchRequest;
import delta.codecharacter.server.controller.response.Simulation.PrivateSimulateMatchResponse;
import delta.codecharacter.server.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@Controller
public class SimulationController {

    private final Logger LOG = Logger.getLogger(SimulationController.class.getName());

    @Autowired
    private SimulationService simulationService;

    @MessageMapping("/matchRequest")
    public void simulateMatch(@RequestBody @Valid SimulateMatchRequest simulateMatchRequest) throws IOException, TimeoutException {
        simulationService.simulateMatch(simulateMatchRequest);
    }

    @SendTo("/socket/{userId}")
    public String sendSocketMessage(@DestinationVariable Integer userId, String message) {
        return message;
    }
}
