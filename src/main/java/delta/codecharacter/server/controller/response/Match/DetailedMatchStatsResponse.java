package delta.codecharacter.server.controller.response.Match;

import delta.codecharacter.server.util.MatchStats;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class DetailedMatchStatsResponse {

    private Integer userId;

    private Integer numMatches;

    private Date lastMatchAt;

    // Matches initiated by user
    private MatchStats initiated;

    // Matches faced by user
    private MatchStats faced;

    // Matches played as auto
    private MatchStats auto;
}
