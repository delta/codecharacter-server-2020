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


    //matches with user as player1

    private Long initiatedWins;

    private Long initiatedLosses;

    private Long initiatedTies;

    //matches with user as player2

    private Long facedWins;

    private Long facedLosses;

    private Long facedTies;


    //matches with user as AUTO_PLAYER

    private Long autoWins;

    private Long autoLosses;

    private Long autoTies;
}
