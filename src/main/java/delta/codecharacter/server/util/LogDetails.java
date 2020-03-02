package delta.codecharacter.server.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogDetails {
    private String gameLog;

    private String player1Log;

    private String player2Log;
}
