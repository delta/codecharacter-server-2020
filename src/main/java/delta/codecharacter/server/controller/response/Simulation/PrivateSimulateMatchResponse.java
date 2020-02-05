package delta.codecharacter.server.controller.response.Simulation;

import delta.codecharacter.server.util.Verdict;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrivateSimulateMatchResponse {
    private Integer playerId1;

    private Integer playerId2;

    private Verdict verdict;

    private Integer score1;

    private Integer score2;
}
