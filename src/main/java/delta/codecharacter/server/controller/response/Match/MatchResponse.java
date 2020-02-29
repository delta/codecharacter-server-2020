package delta.codecharacter.server.controller.response.Match;

import delta.codecharacter.server.util.enums.MatchMode;
import delta.codecharacter.server.util.enums.Status;
import delta.codecharacter.server.util.enums.Verdict;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class MatchResponse {
    private Integer id;

    private Integer playerId1;

    private Integer playerId2;

    private Verdict verdict;

    private Status status;

    private Integer score1;

    private Integer score2;

    private MatchMode matchMode;

    private Date createdAt;
}
