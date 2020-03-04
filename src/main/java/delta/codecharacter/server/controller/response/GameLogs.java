package delta.codecharacter.server.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameLogs {
    private String gameLog;

    private String player1Log;

    private String player2Log;
}
