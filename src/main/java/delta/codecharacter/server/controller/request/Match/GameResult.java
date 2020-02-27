package delta.codecharacter.server.controller.request.Match;

import delta.codecharacter.server.util.enums.Verdict;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class GameResult {
    @NotNull
    private Integer key;

    @NotNull
    private String winner;

    @NotNull
    @Builder.Default
    private Verdict winType = Verdict.TIE;

    @NotNull
    private Integer interestingness;

    @NotNull
    private List<Score> scores;
}
