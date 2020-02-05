package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.request.Simulation.SimulateMatchRequest;
import org.springframework.stereotype.Service;

@Service
public class SimulationService {

    public void simulateMatch(SimulateMatchRequest simulateMatchRequest) {
        String player1Code;
        String player2Code;
        switch (simulateMatchRequest.getMatchMode()) {
            case SELF: {

                break;
            }
            case AI: {

                break;
            }
            case PREV_COMMIT: {

                break;
            }
            case PLAYER: {

                break;
            }
            default: {
                throw new IllegalStateException("Unexpected value: " + simulateMatchRequest.getMatchMode());
            }
        }


    }
}
