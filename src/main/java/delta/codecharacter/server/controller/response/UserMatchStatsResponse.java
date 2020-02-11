package delta.codecharacter.server.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserMatchStatsResponse {

    private Integer userId;

    private Long numMatches;

    private Date lastMatchAt;


    //Matches initiated by user

    private Long initiatedWins;

    private Long initiatedLosses;

    private Long initiatedTies;

    //Matches initiated against user

    private Long facedWins;

    private Long facedLosses;

    private Long facedTies;


    //Matches played by user in auto matching

    private Long autoWins;

    private Long autoLosses;

    private Long autoTies;
}
