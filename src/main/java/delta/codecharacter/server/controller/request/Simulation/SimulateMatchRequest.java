package delta.codecharacter.server.controller.request.Simulation;

import delta.codecharacter.server.util.MatchMode;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class SimulateMatchRequest {
    @NotEmpty(message = "PlayerId1 is required")
    String playerId1;

    @NotEmpty(message = "PlayerId2 is required")
    String playerId2;

    @NotEmpty(message = "Match Mode is required")
    MatchMode matchMode;

    Integer aiId;

    Integer codeVersionId;
}
