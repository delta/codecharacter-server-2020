package delta.codecharacter.server.controller.response;

import delta.codecharacter.server.util.UserMatchStatData;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserMatchStatsResponse {

    private Integer userId;

    private Integer numMatches;

    private Date lastMatchAt;

    // Matches initiated by user
    private UserMatchStatData initiated;

    // Matches faced by user
    private UserMatchStatData faced;

    // Matches played as auto
    private UserMatchStatData auto;
}
