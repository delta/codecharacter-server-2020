package delta.codecharacter.server.controller.request.Simulation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecuteMatchRequest {
    Integer matchId;

    String secretKey;

    String dll1;

    String dll2;

    String code1;

    String code2;

    ExecuteGameDetails[] games;
}
