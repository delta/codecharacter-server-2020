package delta.codecharacter.server.controller.response.Game;

import delta.codecharacter.server.util.enums.Verdict;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameResponse {
    private Integer id;

    private Verdict verdict;

    private Integer mapId;
}
