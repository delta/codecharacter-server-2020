package delta.codecharacter.server.controller.request.Simulation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecuteGameDetails {
    String map;

    Integer gameId;
}
