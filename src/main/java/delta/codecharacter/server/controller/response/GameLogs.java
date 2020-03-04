package delta.codecharacter.server.controller.response;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString(includeFieldNames = false)
public class GameLogs {
    private Integer playerId1;

    private String gameLog;

    private String player1Log;

    private String player2Log;
}
