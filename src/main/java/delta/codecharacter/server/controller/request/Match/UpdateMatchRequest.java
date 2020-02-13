package delta.codecharacter.server.controller.request.Match;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class UpdateMatchRequest {
    @NotNull
    private Integer matchId;

    @NotNull
    @Builder.Default
    private Boolean success = false;

    @NotNull
    private List<GameDetails> gameResults;
}
