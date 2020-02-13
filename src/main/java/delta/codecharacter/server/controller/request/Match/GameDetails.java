package delta.codecharacter.server.controller.request.Match;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class GameDetails {
    @NotNull
    private Integer gameId;

    @NotNull
    private GameResult results;

    @NotNull
    @Builder.Default
    private String errorType;
}
