package delta.codecharacter.server.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserMatchStatsResponse {

    private Integer userId;

    private Integer numMatches;

    private Date lastMatchAt;

    //Matches initiated by user
    private Integer initiatedWins;
    private Integer initiatedLosses;
    private Integer initiatedTies;

    //Matches initiated against user
    private Integer facedWins;
    private Integer facedLosses;
    private Integer facedTies;

    //Matches played by user in auto matching
    private Integer autoWins;
    private Integer autoLosses;
    private Integer autoTies;
}
