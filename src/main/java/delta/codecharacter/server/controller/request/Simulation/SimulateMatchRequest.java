package delta.codecharacter.server.controller.request.Simulation;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class SimulateMatchRequest {
    @NotEmpty(message = "PlayerId1 is required")
    String playerId1;

    //playerId2 is aiId, if matchMode is AI
    @NotEmpty(message = "PlayerId2 is required")
    String playerId2;

    @NotEmpty(message = "Match Mode is required")
    String matchMode;

    String commitHash;

    Integer mapId;
}
