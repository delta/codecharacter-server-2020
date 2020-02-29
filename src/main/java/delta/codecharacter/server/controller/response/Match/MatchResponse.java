package delta.codecharacter.server.controller.response.Match;

import com.fasterxml.jackson.annotation.JsonProperty;
import delta.codecharacter.server.controller.response.Game.GameResponse;
import delta.codecharacter.server.util.enums.MatchMode;
import delta.codecharacter.server.util.enums.Verdict;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class MatchResponse {
    private String username1;

    private String username2;

    private Integer avatar1;

    private Integer avatar2;

    private Integer score1;

    private Integer score2;

    private Integer rating1;

    private Integer rating2;

    private Verdict verdict;

    @JsonProperty("match_mode")
    private MatchMode matchMode;

    private Date playedAt;

    private List<GameResponse> games;
}
