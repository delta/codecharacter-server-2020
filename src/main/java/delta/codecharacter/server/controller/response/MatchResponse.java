package delta.codecharacter.server.controller.response;

import delta.codecharacter.server.util.MatchMode;
import delta.codecharacter.server.util.Status;
import delta.codecharacter.server.util.Verdict;
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
