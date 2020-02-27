package delta.codecharacter.server.controller.request.Match;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class ExecuteMatchRequest {
    @NotNull
    @Builder.Default
    private Boolean success = false;

    @NotNull
    private Integer matchId;

    private List<GameDetails> gameResults;

    private String error;

    private String errorType;
}
