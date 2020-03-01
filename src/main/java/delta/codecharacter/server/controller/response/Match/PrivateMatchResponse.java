package delta.codecharacter.server.controller.response.Match;

import delta.codecharacter.server.model.Game;
import delta.codecharacter.server.util.enums.MatchMode;
import delta.codecharacter.server.util.enums.Verdict;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class PrivateMatchResponse {
    private String username1;

    private String username2;

    private Integer avatar1;

    private Integer avatar2;

    private Integer score1;

    private Integer score2;

    private Verdict verdict;

    @Field("match_mode")
    private MatchMode matchMode;

    @Field("played_at")
    private Date playedAt;

    private List<Game> games;
}
