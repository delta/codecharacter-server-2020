package delta.codecharacter.server.controller.request.Match;

import delta.codecharacter.server.util.Verdict;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class GameResult {
    @NotNull
    private Integer key;

    @NotNull
    private Integer winnerUserId;

    @NotNull
    @Builder.Default
    private Verdict winType = Verdict.TIE;

    @NotNull
    private Integer interestingness;

    @NotNull
    private Score scores;
}
